package com.tangem.rdf.normalization;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tangem.rdf.normalization.NDegreeResult;
import com.tangem.rdf.normalization.NQuadSerializer;
import com.tangem.rdf.normalization.SerializedQuad;
import com.tangem.JavaOver8Utils;
import com.tangem.rdf.Rdf;
import com.tangem.rdf.RdfDataset;
import com.tangem.rdf.RdfNQuad;
import com.tangem.rdf.RdfResource;
import com.tangem.rdf.RdfValue;

import static com.tangem.JavaOver8Utils.DataSet.compare;
import static com.tangem.JavaOver8Utils.isBlank;

/**
 * Perform RDF normalization.
 *
 * @author Simon Greatrix on 05/10/2020.
 */
public class RdfNormalize {

  /**
   * The lower-case hexadecimal alphabet.
   */
  private static final char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


  /**
   * Convert bytes to hexadecimal.
   *
   * @param data the bytes
   *
   * @return the data represented in hexadecimal.
   */
  static String hex(byte[] data) {
    StringBuilder builder = new StringBuilder(data.length * 2);
    for (byte b : data) {
      builder.append(HEX[(b & 0xf0) >> 4]).append(HEX[b & 0xf]);
    }
    return builder.toString();
  }


  /**
   * Normalize an RDF dataset using the URDNA 2015 algorithm.
   *
   * @param input the dataset to be normalized
   *
   * @return a new normalized equivalent dataset.
   */
  public static com.tangem.rdf.RdfDataset normalize(com.tangem.rdf.RdfDataset input) {
    return new RdfNormalize(input).doNormalize();
  }


  /**
   * Normalize an RDF dataset using the specified algorithm. NB. Currently only "URDNA2015" is supported.
   *
   * @param input     the dataset to be normalized
   * @param algorithm the normalization algorithm
   *
   * @return a new normalized equivalent dataset.
   */
  public static com.tangem.rdf.RdfDataset normalize(com.tangem.rdf.RdfDataset input, String algorithm) throws NoSuchAlgorithmException {
    if (algorithm == null || JavaOver8Utils.isBlank(algorithm) || algorithm.equalsIgnoreCase("urdna2015")) {
      // use default algorithm
      return new RdfNormalize(input).doNormalize();
    }

    throw new NoSuchAlgorithmException("Normalization algorithm is not supported:" + algorithm);
  }


  /**
   * The state information for the hash n-degree quads algorithm.
   */
  private class HashNDegreeQuads {

    /** The currently chosen identifier issuer. */
    IdentifierIssuer chosenIssuer = null;

    /** The currently chosen path. */
    StringBuilder chosenPath = null;

    /** The data which will go into the hash. */
    final StringBuilder dataToHash = new StringBuilder();


    /**
     * Append an ID to the hash path.
     *
     * @param related       the ID to append
     * @param pathBuilder   the path to append to
     * @param issuerCopy    the identifier issuer
     * @param recursionList the node recursion list
     */
    private void appendToPath(com.tangem.rdf.RdfResource related, StringBuilder pathBuilder, IdentifierIssuer issuerCopy, List<com.tangem.rdf.RdfResource> recursionList) {
      if (canonIssuer.hasId(related)) {
        // 5.4.4.1: Already has a canonical ID so we just use it.
        pathBuilder.append(canonIssuer.getId(related).getValue());
      } else {
        // 5.4.4.2: Need to try an ID, and possibly recurse
        if (!issuerCopy.hasId(related)) {
          recursionList.add(related);
        }
        pathBuilder.append(issuerCopy.getId(related).getValue());
      }
    }


    /**
     * Implementation of steps 1 to 3 of the Hash N-Degree Quads algorithm.
     *
     * @param id     the ID of the blank node to process related nodes for
     * @param issuer the ID issuer currently being used.
     *
     * @return the required mapping
     */
    private SortedMap<String, Set<com.tangem.rdf.RdfResource>> createHashToRelated(com.tangem.rdf.RdfResource id, IdentifierIssuer issuer) {
      SortedMap<String, Set<com.tangem.rdf.RdfResource>> hashToRelated = new TreeMap<>();
      // quads that refer to the blank node.
      com.tangem.rdf.RdfDataset refer = blankIdToQuadSet.get(id);
      for (com.tangem.rdf.RdfNQuad quad : refer.toList()) {
        // find all the blank nodes that refer to this node by a quad
        for (Position position : Position.CAN_BE_BLANK) {
          if (position.isBlank(quad) && !id.equals(position.get(quad))) {
            com.tangem.rdf.RdfResource related = (com.tangem.rdf.RdfResource) position.get(quad);
            String hash = hashRelatedBlankNode(related, quad, issuer, position);
            hashToRelated.computeIfAbsent(hash, h -> new HashSet<>()).add(related);
          }
        }
      }
      return hashToRelated;
    }


    /**
     * Process one possible permutation of the blank nodes.
     *
     * @param permutation the permutation
     * @param issuer      the identifier issuer
     */
    private void doPermutation(com.tangem.rdf.RdfResource[] permutation, IdentifierIssuer issuer) {
      // 5.4.1 to 5.4.3 : initialise variables
      IdentifierIssuer issuerCopy = issuer.copy();
      StringBuilder pathBuilder = new StringBuilder();
      List<com.tangem.rdf.RdfResource> recursionList = new ArrayList<>();

      // 5.4.4: for every resource in the this permutation of the resources
      for (com.tangem.rdf.RdfResource related : permutation) {
        appendToPath(related, pathBuilder, issuerCopy, recursionList);

        // 5.4.4.3: Is this path better than our chosen path?
        if (chosenPath.length() > 0 && JavaOver8Utils.DataSet.compare(pathBuilder, chosenPath) > 0) {
          // This is permutation is not going to make the best path, so skip the rest of it
          return;
        }
      }

      // 5.4.5: Process the recursion list
      for (com.tangem.rdf.RdfResource related : recursionList) {
        com.tangem.rdf.normalization.NDegreeResult result = hashNDegreeQuads(related, issuerCopy);

        pathBuilder
            .append(issuerCopy.getId(related).getValue())
            .append('<')
            .append(result.getHash())
            .append('>');
        issuerCopy = result.getIssuer();

        if (chosenPath.length() > 0 && JavaOver8Utils.DataSet.compare(pathBuilder, chosenPath) > 0) {
          // This is permutation is not going to make the best path, so skip the rest of it
          return;
        }
      }

      // 5.4.6: Do we have a new chosen path?
      if (chosenPath.length() == 0 || JavaOver8Utils.DataSet.compare(pathBuilder, chosenPath) < 0) {
        chosenPath.setLength(0);
        chosenPath.append(pathBuilder);
        chosenIssuer = issuerCopy;
      }
    }


    /**
     * Calculate the hash from the N-Degree nodes.
     *
     * @param id     the blank node starting ID
     * @param issuer the identifier issuer
     *
     * @return the result
     */
    com.tangem.rdf.normalization.NDegreeResult hash(com.tangem.rdf.RdfResource id, IdentifierIssuer issuer) {
      SortedMap<String, Set<com.tangem.rdf.RdfResource>> hashToRelated = createHashToRelated(id, issuer);

      for (Entry<String, Set<com.tangem.rdf.RdfResource>> entry : hashToRelated.entrySet()) {
        // 5.1 to 5.3: Append the hash for the related item to the hash we are building and initialise variables
        dataToHash.append(entry.getKey());
        chosenPath = new StringBuilder();
        chosenIssuer = null;

        // 5.4: For every possible permutation of the blank node list...
        Permutator permutator = new Permutator(entry.getValue().toArray(new com.tangem.rdf.RdfResource[entry.getValue().size()]));
        while (permutator.hasNext()) {
          doPermutation(permutator.next(), issuer);
        }

        // 5.5: Append chosen path to the hash
        dataToHash.append(chosenPath);
        issuer = chosenIssuer;
      }

      sha256.reset();
      String hash = hex(sha256.digest(dataToHash.toString().getBytes(StandardCharsets.UTF_8)));
      return new com.tangem.rdf.normalization.NDegreeResult(hash, issuer);
    }


    /**
     * Create a hash of the related blank nodes, as described in the specification.
     *
     * @param related  the ID nodes are related to
     * @param quad     the quad to process
     * @param issuer   the identifier issuer
     * @param position the position in the quad
     *
     * @return the hash
     */
    private String hashRelatedBlankNode(
        com.tangem.rdf.RdfResource related,
        com.tangem.rdf.RdfNQuad quad,
        IdentifierIssuer issuer,
        Position position
    ) {
      // Find an ID for the blank ID
      String id;
      if (canonIssuer.hasId(related)) {
        id = canonIssuer.getId(related).getValue();
      } else if (issuer.hasId(related)) {
        id = issuer.getId(related).getValue();
      } else {
        id = hashFirstDegree(related);
      }

      // Create the hash of position, predicate and ID.
      sha256.reset();
      sha256.update(position.tag());
      if (position != Position.GRAPH) {
        sha256.update((byte) '<');
        sha256.update(quad.getPredicate().getValue().getBytes(StandardCharsets.UTF_8));
        sha256.update((byte) '>');
      }
      sha256.update(id.getBytes(StandardCharsets.UTF_8));
      return hex(sha256.digest());
    }


  }



  /** Map of blank IDs to all the quads that reference that specific blank ID. */
  private final HashMap<com.tangem.rdf.RdfValue, com.tangem.rdf.RdfDataset> blankIdToQuadSet = new HashMap<>();

  /** Issuer of canonical IDs to blank nodes. */
  private final IdentifierIssuer canonIssuer = new IdentifierIssuer("_:c14n");

  /**
   * Hash to associated IRIs.
   */
  private final TreeMap<String, Set<com.tangem.rdf.RdfResource>> hashToBlankId = new TreeMap<>();

  /** All the quads in the dataset to be processed. */
  private final List<com.tangem.rdf.RdfNQuad> quads;

  /** An instance of the SHA-256 message digest algorithm. */
  private final MessageDigest sha256;

  /** A set of non-normalized values. */
  private HashSet<com.tangem.rdf.RdfValue> nonNormalized;


  private RdfNormalize(com.tangem.rdf.RdfDataset input) {
    try {
      sha256 = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      // The Java specification requires SHA-256 is included, so this should never happen.
      throw new InternalError("SHA-256 is not available", e);
    }
    quads = input.toList();
  }


  private com.tangem.rdf.RdfDataset doNormalize() {
    // Step 1 is done by the constructor.
    // Step 2:
    findBlankNodes();

    // Step 3:
    setNonNormalized();

    // Steps 4 and 5:
    issueSimpleIds();

    // Step 6:
    issueNDegreeIds();

    // Step 7:
    return makeCanonQuads();
  }


  private void findBlankNodes() {
    // Find all the quads that link with a blank node
    for (com.tangem.rdf.RdfNQuad quad : quads) {
      for (Position position : Position.CAN_BE_BLANK) {
        com.tangem.rdf.RdfValue value = position.get(quad);
        if (value != null && value.isBlankNode()) {
          blankIdToQuadSet.computeIfAbsent(value, k -> com.tangem.rdf.Rdf.createDataset()).add(quad);
        }
      }
    }
  }


  private String hashFirstDegree(com.tangem.rdf.RdfValue blankId) {
    List<com.tangem.rdf.RdfNQuad> related = blankIdToQuadSet.get(blankId).toList();
    String[] nQuads = new String[related.size()];
    int i = 0;

    // Convert the NQuads to a consistent set by replacing the reference with _:a and all others with _:z, and then sorting
    for (com.tangem.rdf.RdfNQuad q0 : related) {
      nQuads[i] = NQuadSerializer.forBlank(q0, blankId);
      i++;
    }

    // Sort the nQuads
    Arrays.sort(nQuads);

    // Create the hash
    sha256.reset();
    for (String s : nQuads) {
      sha256.update(s.getBytes(StandardCharsets.UTF_8));
    }
    return hex(sha256.digest());
  }


  private com.tangem.rdf.normalization.NDegreeResult hashNDegreeQuads(com.tangem.rdf.RdfResource id, IdentifierIssuer issuer) {
    return new HashNDegreeQuads().hash(id, issuer);
  }


  private void issueNDegreeIds() {
    for (Entry<String, Set<com.tangem.rdf.RdfResource>> entry : hashToBlankId.entrySet()) {
      List<com.tangem.rdf.normalization.NDegreeResult> hashPathList = new ArrayList<>();
      for (com.tangem.rdf.RdfResource id : entry.getValue()) {
        // if we've already assigned a canonical ID for this node, skip it
        if (canonIssuer.hasId(id)) {
          continue;
        }

        // Create a new blank ID issuer and assign it's first ID to the reference id
        IdentifierIssuer blankIssuer = new IdentifierIssuer("_:b");
        blankIssuer.getId(id);

        com.tangem.rdf.normalization.NDegreeResult path = hashNDegreeQuads(id, blankIssuer);
        hashPathList.add(path);
      }

      hashPathList.sort(Comparator.naturalOrder());
      for (com.tangem.rdf.normalization.NDegreeResult result : hashPathList) {
        result.getIssuer().assign(canonIssuer);
      }
    }
  }


  private void issueSimpleIds() {
    boolean simple = true;
    while (simple) {
      simple = false;
      hashToBlankId.clear();
      for (com.tangem.rdf.RdfValue value : nonNormalized) {
        com.tangem.rdf.RdfResource blankId = (com.tangem.rdf.RdfResource) value;
        String hash = hashFirstDegree(blankId);
        hashToBlankId.computeIfAbsent(hash, k -> new HashSet<>()).add(blankId);
      }

      Iterator<Entry<String, Set<com.tangem.rdf.RdfResource>>> iterator = hashToBlankId.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<String, Set<com.tangem.rdf.RdfResource>> entry = iterator.next();
        Set<com.tangem.rdf.RdfResource> values = entry.getValue();
        if (values.size() == 1) {
          com.tangem.rdf.RdfResource id = values.iterator().next();
          canonIssuer.getId(id);
          nonNormalized.remove(id);
          iterator.remove();
          simple = true;
        }
      }
    }
  }


  private com.tangem.rdf.RdfDataset makeCanonQuads() {
    com.tangem.rdf.normalization.SerializedQuad[] outputQuads = new com.tangem.rdf.normalization.SerializedQuad[quads.size()];
    int i = 0;
    AtomicBoolean changed = new AtomicBoolean();
    for (RdfNQuad q : quads) {
      changed.set(false);
      com.tangem.rdf.RdfResource subject = canonIssuer.getIfExists(q.getSubject(), changed);
      RdfValue object = canonIssuer.getIfExists(q.getObject(), changed);
      RdfResource graph = canonIssuer.getIfExists(q.getGraphName().orElse(null), changed);

      if (changed.get()) {
        outputQuads[i] = new com.tangem.rdf.normalization.SerializedQuad(com.tangem.rdf.Rdf.createNQuad(subject, q.getPredicate(), object, graph));
      } else {
        outputQuads[i] = new com.tangem.rdf.normalization.SerializedQuad(q);
      }
      i++;
    }

    Arrays.sort(outputQuads);
    RdfDataset rdfDataset = Rdf.createDataset();
    for (SerializedQuad quad : outputQuads) {
      rdfDataset.add(quad.getQuad());
    }
    return rdfDataset;
  }


  private void setNonNormalized() {
    nonNormalized = new HashSet<>(blankIdToQuadSet.keySet());
  }

}

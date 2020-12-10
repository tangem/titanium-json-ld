package com.tangem.rdf.normalization;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tangem.rdf.Rdf;
import com.tangem.rdf.RdfResource;
import com.tangem.rdf.RdfValue;

/**
 * An issuer of counted identifiers to map identifiers from one naming scheme to another.
 *
 * @author Simon Greatrix on 06/10/2020.
 */
public class IdentifierIssuer {

  /** Identifiers that have already been issued. */
  private final LinkedHashMap<com.tangem.rdf.RdfResource, com.tangem.rdf.RdfResource> existing = new LinkedHashMap<>();

  /** The prefix for new identifiers. */
  private final String prefix;

  /** Counter for creating new identifiers. */
  private int counter = 0;


  /**
   * Create a new instance.
   *
   * @param prefix the prefix for new identifiers.
   */
  public IdentifierIssuer(String prefix) {
    this.prefix = prefix;
  }


  /**
   * Create a mapping in an other issuer for all identifiers issued by this, in the same order that they were issued by this.
   *
   * @param other the other identifier issuer.
   */
  public void assign(IdentifierIssuer other) {
    existing.forEach((k, v) -> other.getId(k));
  }


  /**
   * Create a copy of this issuer.
   *
   * @return the issuer to copy
   */
  public IdentifierIssuer copy() {
    IdentifierIssuer newIssuer = new IdentifierIssuer(this.prefix);
    newIssuer.existing.putAll(existing);
    newIssuer.counter = counter;
    return newIssuer;
  }


  private com.tangem.rdf.RdfResource getForBlank(com.tangem.rdf.RdfResource value, AtomicBoolean flag) {
    if (hasId(value)) {
      flag.set(true);
      return getId(value);
    }
    return value;
  }


  /**
   * Get or allocate a new ID for the specified old ID.
   *
   * @param id the old ID
   *
   * @return the new ID
   */
  public com.tangem.rdf.RdfResource getId(com.tangem.rdf.RdfResource id) {
    return existing.computeIfAbsent(id, k -> Rdf.createBlankNode(prefix + (counter++)));
  }


  /**
   * Get the resource replaced by a proper blank identifier if appropriate.
   *
   * @param value the resource to check
   * @param flag  set to true if a replacement happens
   *
   * @return the value or the replaced value
   */
  public com.tangem.rdf.RdfResource getIfExists(com.tangem.rdf.RdfResource value, AtomicBoolean flag) {
    return (value != null && value.isBlankNode()) ? getForBlank(value, flag) : value;
  }


  /**
   * Get the resource replaced by a proper blank identifier if appropriate.
   *
   * @param value the resource to check
   * @param flag  set to true if a replacement happens
   *
   * @return the value or the replaced value
   */
  com.tangem.rdf.RdfValue getIfExists(RdfValue value, AtomicBoolean flag) {
    return (value != null && value.isBlankNode()) ? getForBlank((com.tangem.rdf.RdfResource) value, flag) : value;
  }


  /**
   * Does an old ID have an allocated new ID?.
   *
   * @param id the old ID
   *
   * @return true of a new ID has been allocated for this old ID.
   */
  public boolean hasId(RdfResource id) {
    return existing.containsKey(id);
  }

}

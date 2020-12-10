package com.tangem.rdf.normalization;

import com.tangem.rdf.normalization.NQuadSerializer;
import com.tangem.rdf.RdfNQuad;

/**
 * An RDF quad that has been serialized using the NQuad method.
 *
 * @author Simon Greatrix on 08/10/2020.
 */
public class SerializedQuad implements Comparable<SerializedQuad> {

  private final com.tangem.rdf.RdfNQuad quad;
  private final String serialized;


  SerializedQuad(com.tangem.rdf.RdfNQuad quad) {
    this.quad = quad;
    serialized = NQuadSerializer.write(quad);
  }


  @Override
  public int compareTo(SerializedQuad o) {
    return serialized.compareTo(o.serialized);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SerializedQuad)) {
      return false;
    }

    return getSerialized().equals(((SerializedQuad) o).getSerialized());
  }


  public RdfNQuad getQuad() {
    return quad;
  }


  public String getSerialized() {
    return serialized;
  }


  @Override
  public int hashCode() {
    return getSerialized().hashCode();
  }

}

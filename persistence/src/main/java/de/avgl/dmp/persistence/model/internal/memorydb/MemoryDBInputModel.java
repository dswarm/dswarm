package de.avgl.dmp.persistence.model.internal.memorydb;

import org.culturegraph.mf.types.Triple;


public class MemoryDBInputModel {

	private final Triple triple;

	public MemoryDBInputModel(final Triple triple) {

		this.triple = triple;
	}

	public Triple getTriple() {

		return triple;
	}
}

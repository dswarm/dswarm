package de.avgl.dmp.persistence.model.utils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;

public class BeanReferenceDeserializer extends BeanDeserializer {
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Constructor used by {@link BeanDeserializerBuilder}.
	 */
	public BeanReferenceDeserializer(BeanDeserializerBuilder builder, BeanDescription beanDesc, BeanPropertyMap properties,
			Map<String, SettableBeanProperty> backRefs, HashSet<String> ignorableProps, boolean ignoreAllUnknown, boolean hasViews) {
		
		super(builder, beanDesc, properties, backRefs, ignorableProps, ignoreAllUnknown, hasViews);
	}

	protected BeanReferenceDeserializer(BeanDeserializerBase src) {

		super(src);
	}

	@Override
	public Object deserialize(final JsonParser jp, final DeserializationContext ctxt, final Object bean) throws IOException, JsonProcessingException {
		// TODO Auto-generated method stub
		return super.deserialize(jp, ctxt, bean);
	}
}

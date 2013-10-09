package de.avgl.dmp.controller.jsonschema;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.parser.XSOMParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.avgl.dmp.persistence.model.jsonschema.JSArray;
import de.avgl.dmp.persistence.model.jsonschema.JSElement;
import de.avgl.dmp.persistence.model.jsonschema.JSNull;
import de.avgl.dmp.persistence.model.jsonschema.JSObject;
import de.avgl.dmp.persistence.model.jsonschema.JSOther;
import de.avgl.dmp.persistence.model.jsonschema.JSRoot;
import de.avgl.dmp.persistence.model.jsonschema.JSString;


public class JsonSchemaParser {

	private final XSOMParser parser = new XSOMParser();

	private List<JSElement> iterateParticle(final XSParticle particle) {

		final XSTerm term = particle.getTerm();

		if (term.isModelGroup()) {

			final XSModelGroup modelGroup = term.asModelGroup();
			return iterateModelGroup(modelGroup);
		}

		final ArrayList<JSElement> jsElements = new ArrayList<JSElement>(1);
		jsElements.add(iterateSingleParticle(particle));

		return jsElements;
	}

	private JSElement iterateSingleParticle(final XSParticle particle) {

		final XSTerm term = particle.getTerm();
		if (term.isElementDecl()) {

			final XSElementDecl xsElementDecl = term.asElementDecl();

			final JSElement element = iterateElement(xsElementDecl);

			return particle.isRepeated()? new JSArray(element) : element;
		} else if (term.isModelGroupDecl()) {

			final XSModelGroupDecl xsModelGroupDecl = term.asModelGroupDecl();
			final String name = xsModelGroupDecl.getName();

			final List<JSElement> elements = iterateModelGroup(xsModelGroupDecl.getModelGroup());

			return new JSObject(name, elements);

		} else if (term.isWildcard() && term instanceof XSWildcard.Other) {

			final XSWildcard.Other xsWildcardOther = (XSWildcard.Other) term;

			return new JSOther("wildcard", xsWildcardOther.getOtherNamespace());
		}

		return new JSNull("null");
	}

	private List<JSElement> iterateModelGroup(final XSModelGroup modelGroup) {

		final List<JSElement> list = new ArrayList<JSElement>();

		for (final XSParticle xsParticle : modelGroup) {
			if (xsParticle.getTerm().isModelGroup()) {

				list.addAll(iterateParticle(xsParticle));
			} else {

				list.add(iterateSingleParticle(xsParticle));
			}
		}

		return list;
	}

	private JSElement iterateElement(final XSElementDecl elementDecl) {

		final XSType xsElementDeclType = elementDecl.getType();

		if (xsElementDeclType.isSimpleType()) {

			return iterateSimpleType(xsElementDeclType.asSimpleType()).withName(elementDecl.getName());

		} else if (xsElementDeclType.isComplexType()) {

			final XSSimpleType type = xsElementDeclType.asComplexType().getContentType().asSimpleType();

			if (type != null) {

				return iterateSimpleType(type).withName(elementDecl.getName());
			}

			final JSObject jsElements = new JSObject(elementDecl.getName());

			final List<JSElement> elements = iterateComplexType(xsElementDeclType.asComplexType());

			if (elements.size() == 1 && elements.get(0) instanceof JSOther) {

				return elements.get(0).withName(jsElements.getName());
			}

			jsElements.addAll(elements);

			return jsElements;
		}

		return new JSNull(elementDecl.getName());
	}

	private List<JSElement> iterateComplexType(final XSComplexType complexType) {

		final ArrayList<JSElement> result = new ArrayList<JSElement>();

		final XSContentType contentType = complexType.getContentType();

		final XSParticle xsParticle = contentType.asParticle();
		if (xsParticle != null) {

			result.addAll(iterateParticle(xsParticle));
		} else {
			final XSSimpleType xsSimpleType = contentType.asSimpleType();
			if (xsSimpleType != null) {

				result.add(iterateSimpleType(xsSimpleType));
			}
		}

		final Collection<? extends XSAttributeUse> attributeUses = complexType.getAttributeUses();

		for (final XSAttributeUse attributeUse : attributeUses) {
			final XSAttributeDecl attributeUseDecl = attributeUse.getDecl();
			final XSSimpleType type = attributeUseDecl.getType();

			result.add(iterateSimpleType(type).withName("@" + attributeUseDecl.getName()));
		}

		return result;
	}

	private JSElement iterateSimpleType(final XSSimpleType simpleType) {

//		if (simpleType.isUnion()) {
//			final XSUnionSimpleType xsSimpleTypes = simpleType.asUnion();
//
//			final JSObject jsElements = new JSObject(simpleType.getName());
//
//			for (XSSimpleType xsSimpleType : xsSimpleTypes) {
//
//				jsElements.add(iterateSimpleType(xsSimpleType));
//			}
//
////			return jsElements;
//		}

//		if (simpleType.isList()) {
//
//			final XSListSimpleType xsListSimpleType = simpleType.asList();
//
//			final XSSimpleType itemType = xsListSimpleType.getItemType();
//
////			return iterateSimpleType(itemType).withName(xsListSimpleType.getName());
//		}

//		if (simpleType.getPrimitiveType() != null) {
//
//			final XSSimpleType primitiveType = checkNotNull(simpleType.getPrimitiveType());
//
//			System.out.println(String.format("Primitive type [%s] for element [%s]", primitiveType.getName(), simpleType.getName()));
//
////			return new JSString(simpleType.getName());
//		} else {
//
//			System.out.println("simpleType = " + simpleType);
//		}

		return new JSString(simpleType.getName());
	}

	public void parse(final InputStream is) throws SAXException {
		parser.parse(is);
	}

	public void parse(final Reader reader) throws SAXException {
		parser.parse(reader);
	}

	public void parse(final File schema) throws SAXException, IOException {
		parser.parse(schema);
	}

	public void parse(final URL url) throws SAXException {
		parser.parse(url);
	}

	public void parse(final String systemId) throws SAXException {
		parser.parse(systemId);
	}

	public void parse(final InputSource source) throws SAXException {
		parser.parse(source);
	}

	public JSRoot apply(final String rootName) throws IOException, SAXException {

		final XSSchemaSet result = parser.getResult();

		final Iterator<XSSchema> xsSchemaIterator = result.iterateSchema();

		final JSRoot root = new JSRoot(rootName);

		while (xsSchemaIterator.hasNext()) {
			final XSSchema xsSchema = xsSchemaIterator.next();
			final Iterator<XSElementDecl> xsElementDeclIterator = xsSchema.iterateElementDecls();

			while (xsElementDeclIterator.hasNext()) {

				final XSElementDecl elementDecl = xsElementDeclIterator.next();
				final JSElement element = iterateElement(elementDecl);

				root.add(element);

			}
		}

		return root;
	}
}


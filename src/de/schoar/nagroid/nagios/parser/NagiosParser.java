package de.schoar.nagroid.nagios.parser;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import de.schoar.nagroid.nagios.NagiosSite;

abstract public class NagiosParser {

	protected NagiosSite mNagiosSite;

	public NagiosParser(NagiosSite site) {
		mNagiosSite = site;
	}

	public abstract void updateProblems() throws NagiosParsingFailedException;

	protected Document getDocument(InputStream is)
			throws NagiosParsingFailedException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(is);
		} catch (Exception e) {
			throw new NagiosParsingFailedException("Failed to parse: "
					+ e.getLocalizedMessage(), e);
		}
	}

	public static String getNodeValue(Node n) {
		if (n == null) {
			return null;
		}
		if (!n.hasChildNodes()) {
			return null;
		}
		return n.getFirstChild().getNodeValue();
	}

	public static String getNodeAttribute(Node n, String attr) {
		if (n == null) {
			return null;
		}
		NamedNodeMap nodes = n.getAttributes();
		if (nodes == null) {
			return null;
		}
		Node node = nodes.getNamedItem(attr);
		if (node == null) {
			return null;
		}
		return node.getNodeValue();
	}
}

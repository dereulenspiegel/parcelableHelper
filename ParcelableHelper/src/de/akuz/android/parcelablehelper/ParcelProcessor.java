package de.akuz.android.parcelablehelper;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import de.akuz.android.parcelablehelper.annotations.Marshall;
import de.akuz.android.parcelablehelper.annotations.Parcelable;

@SupportedAnnotationTypes(value = { "de.akuz.android.parcelablehelper.annotations.Parcelable" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ParcelProcessor extends AbstractProcessor {

	private Sheriff currentSheriff;

	private Properties props;
	private VelocityEngine ve;

	private void init() throws IOException {
		props = new Properties();
		URL url = this.getClass().getClassLoader()
				.getResource("velocity.properties");
		props.load(url.openStream());
		ve = new VelocityEngine(props);
		ve.init();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment env) {
		try {
			init();
			currentSheriff = new Sheriff();
			for (Element e : env.getElementsAnnotatedWith(Parcelable.class)) {
				processElement(e);
			}
			JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
					currentSheriff.packageName + "." + currentSheriff.className
							+ "Creator");
			writeSource(jfo.openWriter());
			return true;
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
	}

	private void processElement(Element e) {
		if (e.getKind().isClass()) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
					"Found annotation on class " + e.getSimpleName());
			processClass(e);
		}
	}

	private void writeSource(Writer writer) throws IOException {
		VelocityContext vc = new VelocityContext();
		vc.put("className", currentSheriff.className);
		vc.put("packageName", currentSheriff.packageName);
		vc.put("fields", currentSheriff.fieldsToMarshall);
		Template tem = ve.getTemplate("creatorTemplate.vm");
		tem.merge(vc, writer);
		writer.close();
	}

	private void processClass(Element classElement) {
		TypeElement type = (TypeElement) classElement;
		currentSheriff.className = type.getSimpleName().toString();
		currentSheriff.packageName = ((PackageElement) type
				.getEnclosingElement()).getQualifiedName().toString();
		currentSheriff.fieldsToMarshall = getFieldsToMarshall(classElement);
	}

	private List<MarshallField> getFieldsToMarshall(Element e) {
		List<? extends Element> childs = e.getEnclosedElements();
		List<Element> fieldsWithAnnotation = new ArrayList<Element>();
		List<Element> fieldsWithoutAnnotation = new ArrayList<Element>();
		boolean hasAnnotation = false;
		for (Element c : childs) {
			if (c.getKind().isField()) {
				VariableElement var = (VariableElement) c;
				if (hasMarshallAnnotation(e)) {
					hasAnnotation = true;
					fieldsWithAnnotation.add(var);
				} else {
					fieldsWithoutAnnotation.add(var);
				}
			}
		}
		if (!hasAnnotation) {
			fieldsWithAnnotation.addAll(fieldsWithoutAnnotation);
		}
		return getMarshallFields(fieldsWithAnnotation);
	}

	private List<MarshallField> getMarshallFields(List<Element> elements) {
		List<MarshallField> fields = new ArrayList<MarshallField>();
		for (Element e : elements) {
			if (isNotTransient(e)) {
				fields.add(new MarshallField(e));
			}
		}

		return fields;
	}

	private boolean isNotTransient(Element e) {
		if (e.getModifiers().contains(Modifier.TRANSIENT)) {
			return false;
		}
		return true;
	}

	private boolean hasMarshallAnnotation(Element e) {
		Marshall annotation = e.getAnnotation(Marshall.class);
		if (annotation != null) {
			return true;
		}
		return false;
	}

}

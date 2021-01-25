package org.logoce.extender.api;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Stream;

public final class AnnotationHandleGroup<T extends Annotation>
{
	private final Class<T> annotationClass;
	private final List<IAdapterHandle.AnnotatedHandle<T>> handles;

	public AnnotationHandleGroup(Class<T> annotationClass, List<IAdapterHandle.AnnotatedHandle<T>> handles)
	{
		this.annotationClass = annotationClass;
		this.handles = handles;
	}

	public Class<T> annotationClass()
	{
		return annotationClass;
	}

	public List<IAdapterHandle.AnnotatedHandle<T>> handles()
	{
		return handles;
	}

	public boolean match(Class<? extends Annotation> classifier)
	{
		return annotationClass == classifier;
	}

	public Stream<IAdapterHandle.AnnotatedHandle<T>> stream()
	{
		return handles.stream();
	}
}

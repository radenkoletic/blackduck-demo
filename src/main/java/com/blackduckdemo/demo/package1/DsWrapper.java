package com.blackduckdemo.demo.package1;

import com.blackduckdemo.demo.package2.DelegatingDataSource;

import java.sql.Wrapper;

import javax.sql.DataSource;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ClassUtils;

/**
 * Unwraps a {@link DataSource} that may have been proxied or wrapped in a custom
 * {@link Wrapper} such as {@link DelegatingDataSource}.
 *
 * @author Tadaya Tsuyukubo
 * @author Stephane Nicoll
 * @since 2.0.7
 */
public final class DsWrapper {

	private static final boolean DELEGATING_DATA_SOURCE_PRESENT = ClassUtils.isPresent(
			"org.springframework.jdbc.datasource.DelegatingDataSource", DsWrapper.class.getClassLoader());

	private DsWrapper() {
	}

	/**
	 * Return an object that implements the given {@code target} type, unwrapping delegate
	 * or proxy if necessary using the specified {@code unwrapInterface}.
	 * @param dataSource the datasource to handle
	 * @param unwrapInterface the interface that the target type must implement
	 * @param target the type that the result must implement
	 * @param <I> the interface that the target type must implement
	 * @param <T> the target type
	 * @return an object that implements the target type or {@code null}
	 * @since 2.3.8
	 * @see Wrapper#unwrap(Class)
	 */
	public static <I, T extends I> T unwrap(DataSource dataSource, Class<I> unwrapInterface, Class<T> target) {
		if (target.isInstance(dataSource)) {
			return target.cast(dataSource);
		}
		I unwrapped = safeUnwrap(dataSource, unwrapInterface);
		if (unwrapped != null && unwrapInterface.isAssignableFrom(target)) {
			return target.cast(unwrapped);
		}
		if (DELEGATING_DATA_SOURCE_PRESENT) {
			DataSource targetDataSource = DelegatingDataSourceUnwrapper.getTargetDataSource(dataSource);
			if (targetDataSource != null) {
				return unwrap(targetDataSource, unwrapInterface, target);
			}
		}
		if (AopUtils.isAopProxy(dataSource)) {
			Object proxyTarget = AopProxyUtils.getSingletonTarget(dataSource);
			if (proxyTarget instanceof DataSource proxyDataSource) {
				return unwrap(proxyDataSource, unwrapInterface, target);
			}
		}
		return null;
	}

	/**
	 * Return an object that implements the given {@code target} type, unwrapping delegate
	 * or proxy if necessary. Consider using {@link #unwrap(DataSource, Class, Class)} as
	 * {@link Wrapper#unwrap(Class) unwrapping} won't be considered if {@code target} is
	 * not an interface.
	 * @param dataSource the datasource to handle
	 * @param target the type that the result must implement
	 * @param <T> the target type
	 * @return an object that implements the target type or {@code null}
	 */
	public static <T> T unwrap(DataSource dataSource, Class<T> target) {
		return unwrap(dataSource, target, target);
	}

	private static <S> S safeUnwrap(Wrapper wrapper, Class<S> target) {
		try {
			if (target.isInterface() && wrapper.isWrapperFor(target)) {
				return wrapper.unwrap(target);
			}
		}
		catch (Exception ex) {
			// Continue
		}
		return null;
	}

	private static class DelegatingDataSourceUnwrapper {

		private static DataSource getTargetDataSource(DataSource dataSource) {
			if (dataSource instanceof DelegatingDataSource delegatingDataSource) {
				return delegatingDataSource.getTargetDataSource();
			}
			return null;
		}

	}

}

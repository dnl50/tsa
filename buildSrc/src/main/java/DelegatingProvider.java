import java.io.Serializable;
import java.util.function.BiFunction;

import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.Nullable;

/**
 * Spring Boot's {@code BuildInfo} Task calls the {@link Object#toString() toString} Method on every value present in
 * the Map of the additional build information properties at task execution time. Therefore, a normal Gradle
 * {@link Provider} does not work, since the {@link Object#toString() toString} method does not return the String
 * representation of the provided value.
 *
 * @implNote Instances of this class must be serializable. Gradle will reject unserializable input properties when
 * calculating the input's fingerprint.
 */
public class DelegatingProvider<T extends Serializable> implements Provider<T>, Serializable {

    /**
     * @implNote Marking this property as transient is not an ideal solution, but the only type-safe one, since
     * {@link Provider} does not implement {@link Serializable}.
     */
    private final transient Provider<T> delegate;

    /**
     * @param delegate
     *     The provider all operations are delegated to, not {@code null}.
     */
    public DelegatingProvider(Provider<T> delegate) {
        this.delegate = delegate;
    }

    /**
     * @return The string representation of the contained value.
     */
    @Override
    public String toString() {
        return delegate.get().toString();
    }

    @Override
    public T get() {
        return delegate.get();
    }

    @Nullable
    @Override
    public T getOrNull() {
        return delegate.getOrNull();
    }

    @Override
    public T getOrElse(T defaultValue) {
        return delegate.getOrElse(defaultValue);
    }

    @Override
    public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
        return delegate.map(transformer);
    }

    @Override
    public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
        return delegate.flatMap(transformer);
    }

    @Override
    public boolean isPresent() {
        return delegate.isPresent();
    }

    @Override
    public Provider<T> orElse(T value) {
        return delegate.orElse(value);
    }

    @Override
    public Provider<T> orElse(Provider<? extends T> provider) {
        return delegate.orElse(provider);
    }

    @Override
    @Deprecated
    public Provider<T> forUseAtConfigurationTime() {
        return delegate.forUseAtConfigurationTime();
    }

    @Override
    public <B, R> Provider<R> zip(Provider<B> provider, BiFunction<T, B, R> biFunction) {
        return delegate.zip(provider, biFunction);
    }

}

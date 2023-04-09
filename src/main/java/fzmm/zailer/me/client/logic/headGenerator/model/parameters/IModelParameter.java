package fzmm.zailer.me.client.logic.headGenerator.model.parameters;

import java.util.Optional;

public interface IModelParameter<T> {

    String id();

    Optional<T> value();

    void setValue(T value);


    /**
     * @return If it is required to request the parameter from the user, returns true.
     */
    boolean isRequested();
}

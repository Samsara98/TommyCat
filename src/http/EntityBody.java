package http;

import java.lang.reflect.Array;
import java.util.Arrays;

public class EntityBody<T> {

    T entityBody;

    public EntityBody(T t) {
        entityBody = t;
    }

    public T getEntityBody() {
        return entityBody;
    }

    public void setEntityBody(T entityBody) {
        this.entityBody = entityBody;
    }

    @Override
    public String toString() {
        if (entityBody.getClass().isArray()) {
            return new String((byte[]) entityBody);
        }
        return entityBody.toString();
    }
}

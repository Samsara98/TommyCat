package http;

import java.nio.charset.StandardCharsets;

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
            return new String((byte[]) entityBody, StandardCharsets.UTF_8);
        }
        return entityBody.toString();
    }
}

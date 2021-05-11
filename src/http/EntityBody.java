package http;

import java.util.Date;

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
        return entityBody.toString();
    }
}

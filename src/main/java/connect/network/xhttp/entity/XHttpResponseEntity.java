package connect.network.xhttp.entity;

public enum XHttpResponseEntity {
    FILE, BEAN();

    private Object entity;

    XHttpResponseEntity() {
    }

    public XHttpResponseEntity setEntity(Object entity) {
        this.entity = entity;
        return this;
    }

    public Object getEntity() {
        return entity;
    }
}

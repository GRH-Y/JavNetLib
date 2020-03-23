package connect.network.xhttp.entity;

public enum XResponseEntity {
    FILE, BEAN();

    private Object entity;

    XResponseEntity() {
    }

    public XResponseEntity setEntity(Object entity) {
        this.entity = entity;
        return this;
    }

    public Object getEntity() {
        return entity;
    }
}

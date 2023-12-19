package com.jav.net.xhttp.joggle;

import java.io.Serializable;
import java.util.LinkedHashMap;

public interface IXHttpRequestEntity extends Serializable {

    LinkedHashMap<Object, Object> getUserRequestProperty();

    byte[] getSendData();
}

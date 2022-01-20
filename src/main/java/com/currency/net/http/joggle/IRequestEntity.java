package com.currency.net.http.joggle;

import java.io.Serializable;
import java.util.LinkedHashMap;

public interface IRequestEntity extends Serializable {

    LinkedHashMap<Object, Object> getRequestProperty();

   byte[] getSendData();
}

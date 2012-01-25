package trb.fps.property;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

public class Property<T> {

    public final String name;
    public final Class type;
    public T value;
    public final ListenerList<PropertyChangeListener> listeners = new ListenerList();
    private final Map<Class, Object> userData = new HashMap();

    public Property(String name, Class type, T initialValue) {
        this.name = name;
        this.type = type;
        this.value = initialValue;        
    }

    public String getName() {
        return name;
    }

    public T get() {
        return value;
    }

    public void set(T v) {
        T old = get();
        this.value = v;
        notifyListeners(new PropertyChangeEvent(this, getName(), old, get()));
    }

    void notifyListeners(PropertyChangeEvent e) {
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChange(e);
        }
    }

    public Class getType() {
        return type;
    }

    public void setUserData(Object object) {
        userData.put(object.getClass(), object);
    }

    public void setUserData(Class type, Object object) {
        userData.put(type, object);
    }

    public <T> T getUserData(Class<T> type) {
        return (T) userData.get(type);
    }
}
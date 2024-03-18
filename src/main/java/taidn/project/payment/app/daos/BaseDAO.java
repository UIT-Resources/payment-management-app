package taidn.project.payment.app.daos;

import java.util.List;
import java.util.Map;

public class BaseDAO <Entity> {
    public List<Entity> getAll() {
        throw new UnsupportedOperationException("Unsupported method");
    }
    public Entity getById(Integer id) {
        throw new UnsupportedOperationException("Unsupported method");
    }
    public List<Entity> getByAttributes(Map<String, String> attributeMap) {
        throw new UnsupportedOperationException("Unsupported method");
    }
    public Entity create(Entity entity) {
        throw new UnsupportedOperationException("Unsupported method");
    }
    public Entity delete(Integer id) {
        throw new UnsupportedOperationException("Unsupported method");
    }
    public Entity update(Entity entity){
        throw new UnsupportedOperationException("Unsupported method");
    };
}

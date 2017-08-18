package ex02.entities;

import java.util.List;


/**
 * All elements read by the parser must implement this interface
 */
public interface IEntity {

    void setParameter(String name, String[] args) throws Exception;

    void postInit(List<IEntity> entities) throws Exception;
}

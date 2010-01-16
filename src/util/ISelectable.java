package util;

import java.nio.channels.Selector;

public interface ISelectable {

    void register(Selector selector) throws Exception;
    void update() throws Exception;

}

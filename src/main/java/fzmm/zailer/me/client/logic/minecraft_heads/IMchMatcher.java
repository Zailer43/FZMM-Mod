package fzmm.zailer.me.client.logic.minecraft_heads;

import fzmm.zailer.me.client.logic.minecraft_heads.model.MchHead;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * This also needs to override the {@link java.lang.Object#equals(Object)} and {@link java.lang.Object#hashCode()} methods
 */
public interface IMchMatcher extends Serializable {

    String name();

    default List<MchHead> filter(Collection<MchHead> headList) {
        return headList.parallelStream() // minecraft-heads has 100k heads and growing, parallelization helps
                .filter(this::test)
                .toList();
    }

    boolean test(MchHead head);
}

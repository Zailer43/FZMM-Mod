/*
package me.zailer.testmod.client.unit_test;

import com.mojang.brigadier.StringReader;
import fzmm.zailer.me.client.command.argument_type.ComponentArgumentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ComponentArgumentTypeTest {

    @Test
    void validDepth() {
        this.depthTest(true, "");
        this.depthTest(true, "[]");
        this.depthTest(true, "[");
        this.depthTest(true, "]");
        this.depthTest(true, "{}");
        this.depthTest(true, "}");
        this.depthTest(true, "{");
        this.depthTest(true, "{foo:'bar'}");
        this.depthTest(true, "[foo='bar']");
        this.depthTest(true, "[foo='bar'");
        this.depthTest(true, "foo='bar'");
        this.depthTest(true, "foo='bar']");
        this.depthTest(true, "[enchantments={levels:{\"minecraft:looting\":3,\"minecraft:sharpness\":5}},attribute_modifiers=[{id:\"gravity\",type:\"gravity\",amount:1.,operation:\"add_multiplied_base\"}]]");
        this.depthTest(true, "[container=[{slot:0,item:{id:\"minecraft:chest\",count:1,components:{\"minecraft:container\":[{slot:0,item:{id:\"minecraft:chest\",count:1,components:{\"minecraft:container\":[{slot:0,item:{id:\"minecraft:chest\",count:1,components:{\"minecraft:container\":[{slot:0,item:{id:\"minecraft:chest\",count:1,components:{\"minecraft:container\":[{slot:0,item:{id:\"minecraft:chest\",count:1,components:{\"minecraft:container\":[{slot:0,item:{id:\"minecraft:diamond\",count:1}}]}}}]}}}]}}}]}}}]}}}]]");
        this.depthTest(true, "[my_component:{value:" + "[".repeat(500) + "]".repeat(500) + "}]");
        this.depthTest(true, "[lore=['" + "{}[]".repeat(1500) + "']]");
        this.depthTest(true, "[lore=['" + "[".repeat(3000) + "]".repeat(3000) + "']]");
    }

    @Test
    void invalidDepth() {
        this.depthTest(false, "[my_component:{value:" + "{".repeat(3000) + "}".repeat(3000) + "}]");
        this.depthTest(false, "[enchantments={levels:{\"minecraft:looting\":3,\"minecraft:sharpness\":5}},attribute_modifiers=[{id:\"gravity\",type:\"gravity\",amount:1.," +
                "my_value:" + "[".repeat(3000) + "]".repeat(3000) + ",operation:\"add_multiplied_base\"}]]");
        this.depthTest(false, "[container=" +
                "[{slot:0,item:{id:\"minecraft:chest\",count:1,components:{\"minecraft:container\":".repeat(500) +
                "[{slot:0,item:{id:\"minecraft:diamond\",count:1}}]" +
                "}}}]".repeat(500) + "]"
        );
    }

    @Test
    void depthWithEscape() {
        this.depthTest(false, "[minecraft:custom_name='{\"bold\":true,\"color\":\"#4A98CC\",\"italic\":false,\"text\":\"Hello " +
                "\\'" + "[".repeat(3000) + "]".repeat(3000) + "\\'\"}']");
    }

    void depthTest(boolean expected, String value) {
        Assertions.assertEquals(expected, ComponentArgumentType.maxDepthCheck(new StringReader(value)));
    }
}
*/

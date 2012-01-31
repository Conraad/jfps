package trb.fps.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import trb.fps.jsg.JsgDeferredRenderer;
import trb.fps.jsg.shader.HemisphereLight;
import trb.fps.jsg.shader.NormalMapping;
import trb.fps.jsg.shader.PointLight;
import trb.fps.property.Property;
import trb.jsg.Shape;
import trb.jsg.State.Material;
import trb.jsg.State.StencilFuncParams;
import trb.jsg.State.StencilOpParams;
import trb.jsg.TreeNode;
import trb.jsg.VertexData;
import trb.jsg.enums.StencilAction;
import trb.jsg.enums.StencilFunc;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.Vec3;

public class DeferredSystem {

    public JsgDeferredRenderer renderer;
    private TreeNode replaceableNode = new TreeNode();
    private Map<PointLightComp, PointLight> pointLightMap = new HashMap();
    private Map<HemisphereLightComp, HemisphereLight> hemisphereLightMap = new HashMap();
    private Map<Box, TreeNode> boxMap = new HashMap();
    private long updateChange;

    public DeferredSystem(JsgDeferredRenderer renderer) {
        this.renderer = renderer;
    }

    public void update() {
        for (Entry<PointLightComp, PointLight>  entry : pointLightMap.entrySet()) {
            Vec3 pos = entry.getKey().getComponent(Transform.class).get().getTranslation();
            entry.getValue().positionWorld.set(pos);
            entry.getValue().setColor(new Vec3(entry.getKey().color.get()));
            entry.getValue().setRadius(entry.getKey().radius.get());
        }
        for (Entry<HemisphereLightComp, HemisphereLight>  entry : hemisphereLightMap.entrySet()) {
            HemisphereLightComp comp = entry.getKey();
            HemisphereLight light = entry.getValue();
            light.setColors(comp.skyColor.get(), comp.groundColor.get());
            light.directionWorld.set(comp.getDirection());

        }
        for (Entry<Box, TreeNode>  entry : boxMap.entrySet()) {
            Box box = entry.getKey();
            TreeNode node = entry.getValue();
            Transform transform = box.getComponent(Transform.class);
            // don't seem to be working to well
            //if (transform.getLastChange() > updateChange) {
                node.setTransform(transform.get());
            //}

            if (box.getSizeChange() > updateChange) {
                VertexData newVertexData = box.createVertexData();
                VertexData vertexData = node.getShape(0).getVertexData();
                vertexData.coordinates.rewind();
                vertexData.coordinates.put(newVertexData.coordinates);
                vertexData.coordinates.rewind();
                vertexData.changed();
            }
        }

        updateChange = Property.changeCounter;
    }

    public void recreate(final EntityList entities) {
        Renderer.invokeLater(new Runnable() {

            public void run() {
                recreateImpl(entities);
            }
        });
    }

    private void recreateImpl(EntityList entities) {
        System.out.println("refresh");
        renderer.lightManager.clear();
        pointLightMap.clear();
        for (PointLightComp lightComp : entities.getComponents(PointLightComp.class)) {
            Vec3 pos = lightComp.getComponent(Transform.class).get().getTranslation();
            Vec3 c = new Vec3(lightComp.color.get());
            float radius = lightComp.radius.get();
            PointLight pointLight = renderer.lightManager.createPointLight(c, pos, radius);
            pointLightMap.put(lightComp, pointLight);
        }

        hemisphereLightMap.clear();
        for (HemisphereLightComp lightComp : entities.getComponents(HemisphereLightComp.class)) {
            HemisphereLight light = renderer.lightManager.createHemisphereLight(
                    new Vec3(lightComp.skyColor.get()), new Vec3(lightComp.groundColor.get()), lightComp.getDirection());
            hemisphereLightMap.put(lightComp, light);
        }

        renderer.basePass.getRootNode().removeChild(replaceableNode);
        for (TreeNode child : replaceableNode.getChildren()) {
            replaceableNode.removeChild(child);
        }
        boxMap.clear();
        replaceableNode = createGeometry(entities, boxMap, null);
        renderer.basePass.getRootNode().addChild(replaceableNode);
    }

    public static TreeNode createGeometry(EntityList entities, Map<Box, TreeNode> boxNodeMap, Map<TreeNode, Box> nodeBoxMap) {
        TreeNode replaceableNode = new TreeNode();
        for (Box box : entities.getComponents(Box.class)) {
            Shape shape = new Shape();
            shape.getState().setCullEnabled(true);
            shape.getState().setMaterial(new Material());
            shape.getState().setStencilTestEnabled(true);
            shape.getState().setStencilFunc(new StencilFuncParams(StencilFunc.ALWAYS, 1, 1));
            shape.getState().setStencilOp(new StencilOpParams(StencilAction.KEEP, StencilAction.KEEP, StencilAction.REPLACE));
            shape.setVertexData(box.createVertexData());
            NormalMapping.apply(shape);
            TreeNode node = new TreeNode(shape);
            node.setTransform(box.getComponent(Transform.class).get());
            replaceableNode.addChild(node);
            //level.physicsLevel.addAsConvexHull(node, false);
            if (boxNodeMap != null) {
                boxNodeMap.put(box, node);
            }
            if (nodeBoxMap != null) {
                nodeBoxMap.put(node, box);
            }
        }
        return replaceableNode;
    }
}
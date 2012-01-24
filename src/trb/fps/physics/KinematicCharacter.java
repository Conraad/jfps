/*
 * HALDEN VR PLATFORM
 *
 * RADIATION MODULE
 *
 * $RCSfile: $
 *
 * Author :
 * Date   :
 * Version: $Revision: $ ($Date: $)
 *
 * (c) 2000-2011 Halden Virtual Reality Centre <http://www.ife.no/vr/>,
 * Institutt for energiteknikk. All rights reserved.
 *
 * This code is the property of Halden VR Centre <vr-info@hrp.no> and may
 * only be used in accordance with the terms of the license agreement
 * granted.
 */

package trb.fps.physics;

import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.bulletphysics.linearmath.Transform;
import javax.vecmath.Vector3f;
import trb.jsg.util.Mat4;
import trb.jsg.util.Vec3;

public class KinematicCharacter {

    public KinematicCharacterController character;
    public PairCachingGhostObject ghostObject;
    private Vector3f CHARACTER_POS = new Vector3f(0.75f, 1, 2);
    private float CHARACTER_RADIUS = 0.5f;

    public KinematicCharacter(PhysicsLevel physicsLevel) {
        Transform startTransform = new Transform();
        startTransform.setIdentity();
        startTransform.origin.set(CHARACTER_POS);

        ghostObject = new PairCachingGhostObject();
        ghostObject.setWorldTransform(startTransform);
        physicsLevel.broadphase.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
        float characterHeight = 2f;
        ConvexShape capsule = new SphereShape(CHARACTER_RADIUS);//new CapsuleShape(CHARACTER_RADIUS, characterHeight);
        //ConvexShape capsule = new CapsuleShape(CHARACTER_RADIUS, characterHeight - CHARACTER_RADIUS * 2);
        ghostObject.setCollisionShape(capsule);
        ghostObject.setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);

        float stepHeight = 0.5f;//0.35f;
        character = new KinematicCharacterController(ghostObject, capsule, stepHeight);

        physicsLevel.dynamicsWorld.addCollisionObject(ghostObject, CollisionFilterGroups.CHARACTER_FILTER,
                (short) (CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.DEFAULT_FILTER));

        physicsLevel.dynamicsWorld.addAction(character);
    }

    public void setFromTo(Vec3 from, Vec3 to) {
        character.warp(from);
        character.setWalkDirection(new Vec3(to).sub_(from).set(1, 0f));
    }

    public Mat4 getTransform() {
        Transform characterWorldTrans = ghostObject.getWorldTransform(new Transform());
        Mat4 t3d = new Mat4();
        t3d.setTranslation(characterWorldTrans.origin);
        t3d.setRotationScale(characterWorldTrans.basis);
        //characterTG.setTransform(t3d);
        return t3d;
    }
}
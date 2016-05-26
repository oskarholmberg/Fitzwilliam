package com.game.bb.gamestates;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 14/05/16.
 */
public class MapBuilder {
    private World world;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer renderer;
    private MapProperties mapProperties;
    private float ppm = B2DVars.PPM;
    public static int MAP_WIDTH = 0;

    public MapBuilder(World world, int mapIndex){
        this.world=world;
        Vector2 cam = new Vector2(PlayState.playState.cam.viewportWidth, PlayState.playState.cam.viewportHeight);
        tiledMap = new TmxMapLoader().load("maps/level" + mapIndex + ".tmx");
        mapProperties = tiledMap.getProperties();

        createBoundary(cam.x / 2, cam.y, cam.x / 2, 1); //top
        createBoundary(cam.x / 2, 0, cam.x / 2, 1); //bottom
        createBoundary(0, cam.y / 2, 1, cam.y / 2); // left
        createBoundary(cam.x, cam.y / 2, 1, cam.y / 2); // right
    }

    private void createBoundary(float xPos, float yPos, float width, float height){
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos / ppm, yPos / ppm);
        bdef.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / ppm, height / ppm);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        // set bits to collide with
        fdef.filter.categoryBits = B2DVars.BIT_GROUND;
        fdef.filter.maskBits = B2DVars.BIT_PLAYER | B2DVars.BIT_OPPONENT | B2DVars.BIT_BULLET | B2DVars.BIT_GRENADE;
        body.createFixture(fdef).setUserData(B2DVars.ID_GROUND);
        shape.dispose();
    }

    public Array<Vector2> getSpawnLocations(){
        Array<Vector2> spawnLocArray = new Array<Vector2>();

        for (MapObject obj : tiledMap.getLayers().get("spawn").getObjects().getByType(RectangleMapObject.class)){
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            spawnLocArray.add(new Vector2(rect.getX() / ppm, rect.getY() / ppm));
        }
        return spawnLocArray;
    }

    public int getMapWidth(){
        return mapProperties.get("width", Integer.class);
    }

    public void buildMap(){
        BodyDef bdef = new BodyDef();
        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        for (MapObject obj : tiledMap.getLayers().get("ground").getObjects().getByType(RectangleMapObject.class)){
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set(rect.getX() / ppm + rect.getWidth() / 2 / ppm, rect.getY() / ppm + rect.getHeight() / 2 / ppm);
            shape.setAsBox(rect.getWidth() / 2 / ppm, rect.getHeight() / 2 / ppm);
            fdef.shape=shape;
            fdef.filter.categoryBits = B2DVars.BIT_GROUND;
            fdef.filter.maskBits = B2DVars.BIT_PLAYER | B2DVars.BIT_BULLET | B2DVars.BIT_OPPONENT
                    | B2DVars.BIT_GRENADE | B2DVars.BIT_ENEMY_ENTITY;
            world.createBody(bdef).createFixture(fdef).setUserData(B2DVars.ID_GROUND);
        }
        for (MapObject obj : tiledMap.getLayers().get("dome").getObjects().getByType(RectangleMapObject.class)){
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set(rect.getX() / ppm + rect.getWidth() / 2 / ppm, rect.getY() / ppm + rect.getHeight() / 2 / ppm);
            shape.setAsBox(rect.getWidth() / 2 / ppm, rect.getHeight() / 2 / ppm);
            fdef.shape=shape;
            fdef.filter.categoryBits = B2DVars.BIT_DOME;
            fdef.filter.maskBits = B2DVars.BIT_BULLET | B2DVars.BIT_GRENADE;
            world.createBody(bdef).createFixture(fdef).setUserData(B2DVars.ID_DOME);
        }
        shape.dispose();
        renderer = new OrthogonalTiledMapRenderer(tiledMap);
    }

    public void update(){

    }

    public void render(){
        renderer.setView(PlayState.playState.cam);
        renderer.render();
    }
}

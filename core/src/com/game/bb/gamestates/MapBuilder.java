package com.game.bb.gamestates;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 14/05/16.
 */
public class MapBuilder {
    private World world;
    private TiledMap tiledMap;
    boolean boundaries;

    public MapBuilder(World world, TiledMap tiledMap, Vector2 cam, boolean boundaries){
        this.world=world;
        this.tiledMap=tiledMap;
        this.boundaries=boundaries;

        if(boundaries) {
            createBoundary(cam.x / 2, cam.y, cam.x / 2, 5); //top
            createBoundary(cam.x / 2, 0, cam.x / 2, 5); //bottom
            createBoundary(0, cam.y / 2, 5, cam.y / 2); // left
            createBoundary(cam.x, cam.y / 2, 5, cam.y / 2); // right
        }
    }

    private void createBoundary(float xPos, float yPos, float width, float height){
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos / B2DVars.PPM, yPos / B2DVars.PPM);
        bdef.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / B2DVars.PPM, height / B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        // set bits to collide with
        fdef.filter.categoryBits = B2DVars.BIT_GROUND;
        fdef.filter.maskBits = B2DVars.BIT_PLAYER | B2DVars.BIT_OPPONENT | B2DVars.BIT_BULLET;
        body.createFixture(fdef).setUserData(B2DVars.ID_GROUND);
    }

    public OrthogonalTiledMapRenderer buildMap(){

        TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get("moonBlocks");

        float ts = layer.getTileWidth();
        for(int row = 0; row < layer.getHeight(); row++) {
            for(int col = 0; col < layer.getWidth(); col++) {
                // get cell
                TiledMapTileLayer.Cell cell = layer.getCell(col, row);

                // check that there is a cell
                if(cell == null) continue;
                if(cell.getTile() == null) continue;

                // create body from cell
                BodyDef bdef = new BodyDef();
                bdef.type = BodyDef.BodyType.StaticBody;
                bdef.position.set((col + 0.5f) * ts / B2DVars.PPM, (row + 0.5f) * ts / B2DVars.PPM);
                ChainShape cs = new ChainShape();
                Vector2[] v = new Vector2[3];
                v[0] = new Vector2(-ts / 2 / B2DVars.PPM, -ts / 2 / B2DVars.PPM);
                v[1] = new Vector2(-ts / 2 / B2DVars.PPM, ts / 2 / B2DVars.PPM);
                v[2] = new Vector2(ts / 2 / B2DVars.PPM, ts / 2 / B2DVars.PPM);
                cs.createChain(v);
                FixtureDef fd = new FixtureDef();
                fd.shape = cs;
                fd.filter.categoryBits = B2DVars.BIT_GROUND;
                fd.filter.maskBits = B2DVars.BIT_PLAYER | B2DVars.BIT_BULLET | B2DVars.BIT_OPPONENT;
                world.createBody(bdef).createFixture(fd).setUserData(B2DVars.ID_GROUND);
                cs.dispose();
            }
        }
        return new OrthogonalTiledMapRenderer(tiledMap);
    }
}

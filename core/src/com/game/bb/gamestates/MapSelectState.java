package com.game.bb.gamestates;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.IntMap;
import com.game.bb.handlers.Assets;

/**
 * Created by erik on 02/06/16.
 */
public class MapSelectState extends GameState {

    private World world;
    private SPButton backButton;
    private Texture background = Assets.getBackground();
    private IntMap<SPButton> mapButtons;

    protected MapSelectState(GameStateManager gsm) {
        super(gsm);
        world = new World(new Vector2(0f, -9.81f), true);
        populateMapSelection();
        backButton = new SPButton(new Texture("images/button/backButton.png"), cam.viewportWidth - 100,
                cam.viewportHeight - 100, 40f, 40f, cam);
    }

    private void populateMapSelection(){
        mapButtons = new IntMap<SPButton>();
        for (int i = 2; i <= 4; i++){
            SPButton mapButton = new SPButton(Assets.getTex("heart"), 200 + 80*i, cam.viewportHeight - 400,
                    50, 50, cam);
            mapButton.setInfo(Integer.toString(i));
            mapButtons.put(i, mapButton);
        }
    }



    @Override
    public void handleInput() {
        if (backButton.isClicked()){
            Assets.getSound("menuSelect").play();
            gsm.setState(GameStateManager.CONNECT);
        }
        for (IntMap.Keys it = mapButtons.keys(); it.hasNext; ){
            int mapIndex = it.next();
            if (mapButtons.get(mapIndex).isClicked()){
                gsm.setMapSelection(mapIndex);
                gsm.setState(GameStateManager.PLAY);
            }
        }
    }

    @Override
    public void update(float dt) {
        world.step(dt, 6, 2);
        backButton.update(dt);
        for (IntMap.Keys it = mapButtons.keys(); it.hasNext;){
            mapButtons.get(it.next()).update(dt);
        }
        handleInput();
    }

    @Override
    public void render() {
        sb.begin();
        sb.draw(background, 0, 0);
        sb.end();
        backButton.render(sb);
        for (IntMap.Keys it = mapButtons.keys(); it.hasNext;){
            mapButtons.get(it.next()).render(sb);
        }

    }

    @Override
    public void dispose() {

    }
}

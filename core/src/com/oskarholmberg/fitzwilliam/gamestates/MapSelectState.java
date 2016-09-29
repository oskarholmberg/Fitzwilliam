package com.oskarholmberg.fitzwilliam.gamestates;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.oskarholmberg.fitzwilliam.handlers.Assets;

import java.util.HashMap;


public class MapSelectState extends GameState {

    private World world;
    private MenuButton backButton;
    private Texture background = Assets.getBackground();
    private HashMap<Integer, MenuButton> mapButtons;

    protected MapSelectState(GameStateManager gsm) {
        super(gsm);
        world = new World(new Vector2(0f, -9.81f), true);
        populateMapSelection();
        backButton = new MenuButton(new Texture("images/button/backButton.png"), cam.viewportWidth - 100,
                cam.viewportHeight - 100, 40f, 40f, cam);
    }

    private void populateMapSelection(){
        mapButtons = new HashMap<Integer, MenuButton>();
        int index = 0;
        for (int i = 3; i <= 4; i++){
            MenuButton mapButton = new MenuButton(new Texture("maps/mapicons/level" +  i + "icon.png")
                    , 350 + 220*index, cam.viewportHeight - 300, 200, 70, cam);
            mapButton.setInfo(Integer.toString(i));
            mapButtons.put(i, mapButton);
            index++;
        }
    }



    @Override
    public void handleInput() {
        if (backButton.isClicked()){
            Assets.getSound("menuSelect").play();
            gsm.setState(GameStateManager.CONNECT);
        }
        for (int index : mapButtons.keySet()){
            if (mapButtons.get(index).isClicked()){
                gsm.setMapSelection(Integer.valueOf(mapButtons.get(index).getInfo()));
                gsm.setState(GameStateManager.PLAY);
            }
        }
    }

    @Override
    public void update(float dt) {
        world.step(dt, 6, 2);
        backButton.update(dt);
        for (int index : mapButtons.keySet()){
            mapButtons.get(index).update(dt);
        }
        handleInput();
    }

    @Override
    public void render() {
        sb.begin();
        sb.draw(background, 0, 0);
        sb.end();
        backButton.render(sb);
        for (int index : mapButtons.keySet()){
            mapButtons.get(index).render(sb);
        }

    }

    @Override
    public void dispose() {
        backButton.dispose();
        for (int index : mapButtons.keySet()){
            mapButtons.get(index).dispose();
        }

    }
}

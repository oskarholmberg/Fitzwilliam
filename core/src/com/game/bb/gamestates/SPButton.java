package com.game.bb.gamestates;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.game.bb.handlers.SPInput;

/**
 * Created by erik on 12/05/16.
 */
public class SPButton {
    private Texture texture;
    private float xPos, yPos, texWidth, texHeight;
    private OrthographicCamera cam;
    private String info="";

    private Vector3 vector3;
    private boolean clicked;

    public SPButton(Texture texture, float xPos, float yPos, OrthographicCamera cam){
        this.texture=texture;
        this.xPos=xPos;
        this.yPos=yPos;
        this.cam=cam;
        texWidth=texture.getWidth();
        texHeight=texture.getHeight();

        vector3 = new Vector3();

    }
    public boolean isClicked(){ return clicked;}

    public void update(float dt){
        vector3.set(SPInput.x, SPInput.y, 0);
        cam.unproject(vector3);
        if(SPInput.isPressed() &&
                vector3.x > xPos - texWidth / 2 && vector3.x < xPos + texWidth / 2 &&
                vector3.y > yPos - texHeight / 2 && vector3.y < yPos + texHeight / 2) {
            clicked = true;
        }
        else {
            clicked = false;
        }
    }

    public void render(SpriteBatch sb){
        sb.begin();
        sb.draw(texture, xPos - texWidth / 2, yPos - texHeight / 2);
        sb.end();
    }

    public void setInfo(String server) {
        info = server;
    }

    public String getInfo(){
        return info;
    }
}

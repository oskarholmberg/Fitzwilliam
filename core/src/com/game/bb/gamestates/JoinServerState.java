package com.game.bb.gamestates;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.game.bb.handlers.Assets;
import com.game.bb.handlers.B2DVars;
import com.game.bb.handlers.GameStateManager;
import com.game.bb.net.client.GameClient;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by erik on 12/05/16.
 */
public class JoinServerState extends GameState {

    private HashMap<InetAddress, SPButton> joinButtons;
    private World world;
    private Texture background = Assets.getBackground();
    private Texture availableServers = new Texture("images/font/availableServers.png");
    private SPButton backbutton;
    private Array<FallingBody> itRains;
    private float newFallingBody = 0f, refresh = 5f;
    private GameClient client;
    private ServerSearcher searcher;
    private TextureRegion[] font;


    public JoinServerState(GameStateManager gsm) {
        super(gsm);
        world = new World(new Vector2(0, -9.81f), true);
        client = new GameClient();
        searcher = new ServerSearcher();
        searcher.start();
        itRains = new Array<FallingBody>();
        joinButtons = new HashMap<InetAddress, SPButton>();
        backbutton = new SPButton(new Texture("images/button/backButton.png"), cam.viewportWidth - 100,
                cam.viewportHeight - 100, 40f, 40f, cam);
        font = new TextureRegion[11];
        Texture hudTex = new Texture("images/hud.png");
        for (int i = 0; i < 6; i++) {
            font[i] = new TextureRegion(hudTex, 32 + i * 9, 16, 9, 9);
        }
        for (int i = 0; i < 5; i++) {
            font[i + 6] = new TextureRegion(hudTex, 32 + i * 9, 25, 9, 9);
        }
        fallingBody();
    }

    public void fallingBody() {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(20 / B2DVars.PPM, 20 / B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        BodyDef bdef = new BodyDef();
        bdef.position.set((float) (cam.viewportWidth * Math.random()), cam.viewportHeight);
        bdef.type = BodyDef.BodyType.DynamicBody;
        Body body = world.createBody(bdef);
        itRains.add(new FallingBody(body));
        shape.dispose();
    }


    @Override
    public void handleInput() {
        if (backbutton.isClicked()) {
            Assets.getSound("menuSelect").play();
            searcher.stopSearch();
            gsm.setState(GameStateManager.CONNECT);
        }
        for (InetAddress address : joinButtons.keySet()) {
            if (joinButtons.get(address).isClicked()) {
                Assets.getSound("menuSelect").play();
                client.connectToServer(address);
                gsm.setClient(client);
                searcher.stopSearch();
                gsm.setState(GameStateManager.PLAY);
            }
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        if (refresh > 0.5f) {
            joinButtons = getJoinButtons();
            refresh = 0;
        } else {
            refresh += dt;
        }
        backbutton.update(dt);
        if (newFallingBody > 1f) {
            fallingBody();
            newFallingBody = 0f;
        } else {
            newFallingBody += dt;
        }
        world.step(dt, 6, 2);
        for (InetAddress address : joinButtons.keySet()) {
            joinButtons.get(address).update(dt);
        }
    }

    @Override
    public void render() {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(background, 0, 0);
        sb.end();
        for (FallingBody body : itRains)
            body.render(sb);
        for (InetAddress address : joinButtons.keySet()) {
            joinButtons.get(address).render(sb);
        }
        backbutton.render(sb);
        sb.begin();
        int k = 0;
        for (InetAddress address : joinButtons.keySet()) {
            SPButton button = joinButtons.get(address);
            int j = 0;
            String[] ip = button.getInfo().split("\\.");
            for (int e = 0; e < ip.length; e++) {
                for (int i = 0; i < ip[e].length(); i++) {
                    sb.draw(font[Integer.valueOf(ip[e].substring(i, i + 1))], 100 + j * 22, (cam.viewportHeight - 180) - 50 * k, 20, 20);
                    j++;
                }
                j++;
            }
            k++;
        }
        sb.draw(availableServers, 100, cam.viewportHeight - 120, 600, 30);
        sb.end();
    }

    @Override
    public void dispose() {
        for (FallingBody b : itRains) {
            b.dispose();
        }
        availableServers.dispose();
        backbutton.dispose();
    }

    private HashMap<InetAddress, SPButton> getJoinButtons() {
        HashMap<InetAddress, SPButton> buttons = new HashMap<InetAddress, SPButton>();
        int i = 0;
        int offset = 0;
        List<InetAddress> servers = searcher.getServers();
        for (InetAddress server : servers) {
            if (servers.size() > 2) offset = 120;
            else offset = 170;
            SPButton button = new SPButton(new Texture("images/button/joinButton.png"), cam.viewportWidth - 350, (cam.viewportHeight - offset) - (50 * i), 100, 20, cam);
            button.setInfo(server.getHostAddress());
            buttons.put(server, button);
            i++;
        }
        return buttons;
    }

    public class FallingBody implements Disposable {
        private Texture texture = Assets.getTex("redJumpLeft");
        private Body body;

        public FallingBody(Body body) {
            this.body = body;
        }

        public void render(SpriteBatch sb) {
            sb.begin();
            sb.draw(texture, body.getPosition().x, body.getPosition().y, 50, 44);
            sb.end();
        }

        @Override
        public void dispose() {
        }
    }

    private class ServerSearcher extends Thread {

        private boolean searching = true;
        private List<InetAddress> serverAddresses;

        private ServerSearcher() {
            serverAddresses = new ArrayList<InetAddress>();
        }

        public void run() {
            while (searching) {
                serverAddresses = client.getLocalServers();
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private List<InetAddress> getServers() {
            return serverAddresses;
        }

        private void stopSearch() {
            searching = false;
        }
    }
}


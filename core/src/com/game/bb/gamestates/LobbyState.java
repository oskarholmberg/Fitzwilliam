package com.game.bb.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.game.bb.handlers.B2DVars;
import com.game.bb.handlers.GameStateManager;
import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by erik on 12/05/16.
 */
public class LobbyState extends GameState {

    private Array<SPButton> joinButtons;
    private Array<String> servers;
    private World world;
    private Texture background = new Texture("images/spaceBackground.png");
    private Texture availableServers = new Texture("images/font/availableServers.png");
    private SPButton backbutton;
    private Sound sound = Gdx.audio.newSound(Gdx.files.internal("sfx/levelselect.wav"));
    private Array<FallingBody> itRains;
    private float newFallingBody = 0f;
    private GameServerSearcher searcher;


    public LobbyState(GameStateManager gsm) {
        super(gsm);
        world = new World(new Vector2(0, -9.81f), true);
        itRains = new Array<FallingBody>();
        joinButtons = new Array<SPButton>();
        backbutton = new SPButton(new Texture("images/button/backButton.png"), cam.viewportWidth - 100,
                cam.viewportHeight - 100, 40f, 40f, cam);
        servers = new Array<String>();
        searcher = new GameServerSearcher();
        searcher.start();
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
            System.out.println("Button clicked");
            searcher.stopSearch();
            System.out.println("Search stopped");
            sound.play();
            System.out.println("Returning to connect mode");
            gsm.setState(GameStateManager.CONNECT);
        }
        for (SPButton joinButton : joinButtons) {
            if (joinButton.isClicked()) {
                dispose();
                gsm.setIpAddress(joinButton.getInfo());
                searcher.stopSearch();
                sound.play();
                sound.dispose();
                gsm.setState(GameStateManager.PLAY);
            }
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        servers = searcher.getServerList();
        joinButtons = getJoinButtons(servers);
        backbutton.update(dt);
        if (newFallingBody > 1f) {
            fallingBody();
            newFallingBody = 0f;
        } else {
            newFallingBody += dt;
        }
        world.step(dt, 6, 2);
        for (SPButton button : joinButtons) {
            button.update(dt);
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
        for (SPButton button : joinButtons) {
            button.render(sb);
        }
        backbutton.render(sb);
        sb.begin();
        sb.draw(availableServers, 100, cam.viewportHeight-120, 600, 30);
        sb.end();
    }

    @Override
    public void dispose() {
        for (FallingBody b : itRains){
            b.dispose();
        }
    }

    private Array<SPButton> getJoinButtons(Array<String> servers) {
        Array<SPButton> buttons = new Array<SPButton>();
        int i = 0;
        for (String server : servers) {
            SPButton button = new SPButton(new Texture("images/button/joinButton.png"), cam.viewportWidth/5, (cam.viewportHeight - 180) - 50 * i, 200f, 20f, cam);
            button.setInfo(server);
            buttons.add(button);
            i++;
        }
        return buttons;
    }

    public class FallingBody implements Disposable{
        private Texture texture = new Texture("images/player/redPlayerJumpLeft.png");
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
            texture.dispose();
        }
    }

    private class GameServerSearcher extends Thread {

        private DatagramSocket socket;
        private String serverAddress;
        private Array<String> serverList;
        private GameServerProbe probe;

        private GameServerSearcher() {
            serverList = new Array<String>();
            try {
                socket = new DatagramSocket();
                probe = new GameServerProbe(socket);
                probe.start();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!socket.isClosed()) {
                try {
                    byte[] serverInfo = new byte[1024];
                    DatagramPacket received = new DatagramPacket(serverInfo, serverInfo.length);
                    socket.receive(received);
                    serverAddress = new String(received.getData()).trim();
                    if (!serverList.contains(serverAddress, false)) {
                        serverList.add(serverAddress);
                        System.out.println("Server found!");
                    }
                    sleep(2000);
                } catch (SocketException e) {
                    stopSearch();
                } catch (UnknownHostException e) {
                    stopSearch();
                } catch (IOException e) {
                    stopSearch();
                } catch (InterruptedException e) {
                    stopSearch();
                }
            }
        }

        public synchronized void stopSearch() {
            socket.close();
            System.out.println("Server search stopped, socket closed.");
        }

        public synchronized Array<String> getServerList() {
            return serverList;
        }
    }

    private class GameServerProbe extends Thread {

        private DatagramSocket socket;

        private GameServerProbe(DatagramSocket socket) {
            this.socket = socket;
        }

        public void run() {
            while (!socket.isClosed()) {
                try {
                    byte[] hello = "HELLO".getBytes();
                    DatagramPacket packet = new DatagramPacket(hello, hello.length, InetAddress.getByName("224.0.13.37"), 8081);
                    socket.send(packet);
                    sleep(2000);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

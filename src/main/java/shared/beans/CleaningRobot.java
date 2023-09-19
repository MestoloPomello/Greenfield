package shared.beans;

import shared.beans.InputRobot;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CleaningRobot extends InputRobot {

    private int posX, posY;

    public CleaningRobot() {
        super();
    };

    public CleaningRobot(int id, int port, String address, int posX, int posY) {
        super(id, port, address);
        this.posX = posX;
        this.posY = posY;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }
}

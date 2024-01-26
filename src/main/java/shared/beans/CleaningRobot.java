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

    public int getDistrictFromPos() {
        // Based on the graph in the PDF
        if (posX <= 4) {
            if (posY <= 4) return 1;
            else return 2;
        }
        else {
            if (posY <= 4) return 4;
            else return 3;
        }
    }

    @Override
    public String toString() {
        return "ID: " + getId() + " | Port: " + getPort() + " | Pos: [" + posX + ", " + posY + "]";
    }
}

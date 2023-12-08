    package administrator_server.beans;

    import shared.beans.RobotListResponse;
    import shared.constants.Constants;
    import shared.beans.CleaningRobot;
    import shared.beans.InputRobot;
    import shared.beans.RobotCreationResponse;
    import shared.exceptions.DuplicatedIdException;
    import shared.exceptions.NoIdException;

    import java.util.ArrayList;
    import java.util.List;

    import javax.xml.bind.annotation.XmlAccessType;
    import javax.xml.bind.annotation.XmlAccessorType;
    import javax.xml.bind.annotation.XmlElement;
    import javax.xml.bind.annotation.XmlRootElement;

    @XmlRootElement
    @XmlAccessorType (XmlAccessType.FIELD)
    public class CleaningRobots {

        @XmlElement(name="deployed_robots")
        private List<CleaningRobot> deployedRobots;

        private final static Object lock = new Object();
        private static CleaningRobots instance;

        private CleaningRobots() {
            deployedRobots = new ArrayList<CleaningRobot>();
        }

        // Singleton
        public static CleaningRobots getInstance(){
            synchronized(lock) {
                if (instance == null)
                    instance = new CleaningRobots();
                return instance;
            }
        }

        public List<CleaningRobot> getDeployedRobots() {
            synchronized (lock) {
                return deployedRobots;
            }
        }

        public void setDeployedRobots(List<CleaningRobot> deployedRobots) {
            synchronized (lock) {
                this.deployedRobots = deployedRobots;
            }
        }

        public RobotCreationResponse insertRobot(InputRobot newRobot){
            boolean isIdDuplicated = false;
            int newId = newRobot.getId();
            int newPort = newRobot.getPort();
            try {
                synchronized (lock) {
                    for (CleaningRobot cr : deployedRobots) {
                        if (cr.getId() == newId) {
                            isIdDuplicated = true;
                            break;
                        }
                    }
                    if (isIdDuplicated) {
                        throw new DuplicatedIdException("Couldn't add a new robot");
                    }

                    // DA SISTEMARE: vanno generati
                    int posX = 0, posY = 0;

                    deployedRobots.add(new CleaningRobot(
                            newId,
                            newRobot.getPort(),
                            Constants.SERVER_ADDR,
                            posX,
                            posY
                    ));

                    return new RobotCreationResponse(
                            newId,
                            newPort,
                            Constants.SERVER_ADDR,
                            posX,
                            posY,
                            Constants.STATUS_SUCCESS,
                            deployedRobots
                    );
                }
            } catch (DuplicatedIdException e) {
                return new RobotCreationResponse(Constants.ERR_DUPED_ID);
            } catch (Exception e) {
                e.printStackTrace();
                return new RobotCreationResponse(Constants.ERR_UNKNOWN);
            }
        }

        public RobotCreationResponse updateRobot(int robotId, CleaningRobot updatedRobot) {
            try {
                boolean foundId = false;

                synchronized (lock) {
                    for (CleaningRobot cr : deployedRobots) {
                        if (cr.getId() == robotId) {
                            foundId = true;
                            cr.setPosX(updatedRobot.getPosX());
                            cr.setPosY(updatedRobot.getPosY());
                            break;
                        }
                    }

                    if (!foundId) throw new NoIdException("Couldn't update the robot");

                    return new RobotCreationResponse(
                            updatedRobot.getId(),
                            updatedRobot.getPort(),
                            Constants.SERVER_ADDR,
                            updatedRobot.getPosX(),
                            updatedRobot.getPosX(),
                            Constants.STATUS_SUCCESS,
                            deployedRobots
                    );
                }
            } catch (NoIdException e) {
                e.printStackTrace();
                return new RobotCreationResponse(Constants.ERR_NO_ID);
            }
        }

        public int deleteRobot(int id) {
            int tbrIndex = -1, index = 0;

            try {
                synchronized (lock) {
                    for (CleaningRobot cr : deployedRobots) {
                        if (cr.getId() == id) {
                            tbrIndex = index;
                            break;
                        }
                        index++;
                    }

                    if (tbrIndex == -1) {
                        throw new NoIdException("Couldn't delete the robot");
                    }

                    deployedRobots.remove(tbrIndex);
                }

                return Constants.STATUS_SUCCESS;
            } catch (NoIdException e) {
                e.printStackTrace();
                return Constants.ERR_NO_ID;
            } catch (Exception e) {
                e.printStackTrace();
                return Constants.ERR_UNKNOWN;
            }
        }

    }

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

    import static shared.utils.Utils.generateCoordinatesForDistrict;
    import static shared.utils.Utils.getRandomInt;

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

                    // Select the district based on the numbers of robots for each one
                    int district = selectDistrict(deployedRobots);
                    // Generate a position within that district
                    int[] position = generateCoordinatesForDistrict(district);

                    deployedRobots.add(new CleaningRobot(
                            newId,
                            newRobot.getPort(),
                            Constants.SERVER_ADDR,
                            position[0],
                            position[1]
                    ));

                    return new RobotCreationResponse(
                            newId,
                            newPort,
                            Constants.SERVER_ADDR,
                            position[0],
                            position[1],
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

        public boolean updatePosition(int robotId, int newPosX, int newPosY) {
            try {
                boolean foundId = false;

                synchronized (lock) {
                    for (CleaningRobot cr : deployedRobots) {
                        if (cr.getId() == robotId) {
                            foundId = true;
                            cr.setPosX(newPosX);
                            cr.setPosY(newPosY);
                            break;
                        }
                    }
                    if (!foundId) throw new NoIdException("Couldn't update the robot's position");
                    return true;
                }
            } catch (NoIdException e) {
                e.printStackTrace();
                return false;
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
                System.out.println("[QUTI] Acknowledged that robot " + id + " has quit.");
                return Constants.STATUS_SUCCESS;
            } catch (NoIdException e) {
                e.printStackTrace();
                return Constants.ERR_NO_ID;
            } catch (Exception e) {
                e.printStackTrace();
                return Constants.ERR_UNKNOWN;
            }
        }

        public void removeCrashedRobot(int robotId) {
            synchronized (lock) {
                deployedRobots.removeIf(cr -> cr != null && cr.getId() == robotId);
            }
            System.out.println("[CRASH] Acknownledged crash of robot with ID " + robotId);
        }


        // Local functions

        private static int selectDistrict(List<CleaningRobot> deployedRobots) {
            // Returns a district based on the number of robots in each one
            int[] districtRobots = new int[4];

            for (CleaningRobot cr : deployedRobots) {
                districtRobots[cr.getDistrictFromPos() - 1]++;
            }

            int minIndex = 0, minNumber = districtRobots[0];
            for (int i = 1; i < 4; i++) {
                if (districtRobots[i] < minNumber) {
                    minNumber = districtRobots[i];
                    minIndex = i;
                }
            }
            return minIndex + 1;
        }

    }

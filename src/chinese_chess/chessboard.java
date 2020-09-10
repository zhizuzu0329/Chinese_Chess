package chinese_chess;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * {@code chessboard} 棋盘类.继承JPenal，实现接口
 */
public class chessboard extends JPanel implements Runnable {
    public static final int RED_PLAYER = 1;
    public static final int BLACK_PLAYER = 0;
    /**
     *  定义一个数组存储32个棋子对象。
     */
    public chess[] chess = new chess[32];
    /**
     *  二维数组map保存了当前棋子的布局.
     */
    public int[][] map = new int[10][9];
    public Image bufferImage;
    /**
     *  第一次点击的棋子
     */
    private chess firstChess = null;
    /**
     *  第二次点击的棋子
     */
    private chess secondChess = null;
    /**
     *  是否第一次点击用isFirstClick表示.
     */
    private boolean isFirstClick = true;
    /**
     *  保存第一次第二次选中的坐标
     */
    private int x1, y1, x2, y2;
    /**
     *   临时变量
     */
    private int tempX, tempY;
    /**
     *  是否自己的回合
     */
    private boolean isMyTurn = true;
    /**
     *  定义当前执子玩家为红方
     */
    public int localPlayer = RED_PLAYER;
    /**
     *  用于发送信息
     */
    private String message = "";
    /**
     *
     */
    private boolean flag = false;
    /**
     *  定义联机电脑的端口
     */
    private int otherPort = 3003;
    /**
     * 定义自己的端口
     */
    private int myPort = 3004;
    /**
     *  声明ArrayList<node>对象list用于保存每步棋的信息.
     *  不必指定起数组的长度，它可以动态的增加素组的长度.
     *  可以存储不同的类型的数据，他会把他所有的元素都当做object来处理.
     */
    public ArrayList<node> list = new ArrayList<node>();
    /**
     *  定义联机目标的ip
     *  ！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！需要修改该值！！！！！！！！！！！！！！！
     *  */
    private String ip = "192.168.137.1";

    /**
     * {@code  initMap()} 对棋盘进行初始化，全部为-1，表示没有棋子.
     */
    private void initMap() {
        int i, j;
        for (i = 0; i < 10; i++) {
            for (j = 0; j < 9; j++) {
                map[i][j] = -1;
            }
        }
    }

    /**
     * {@code  chessboard()}棋盘构造方法：对棋盘进行初始化，接着为棋盘添加鼠标监听.<br/>
     * 监听事件先判断是否自己执子，如果是自己执子，再判断是第几次点击.
     */

    public chessboard() {
        initMap();
       message = "程序处于等待联机状态！";
        //MouseAdapter 适配器
        addMouseListener(new MouseAdapter() {
            @Override
            //单击鼠标时调用
            public void mouseClicked(MouseEvent e) {
                if (!isMyTurn) {
                    message = "现在是对方走棋";
                    repaint();
                    return;
                }
                selectedchess(e);
                repaint();
            }

            /**
             * {@code selectedchess(MouseEvent e)}通过鼠标点击选择棋子<br/>
             * <p>（1）如果是第一次点击{<br/>
             *      由点击处的像素坐标转换为棋盘坐标，并将该坐标上的棋子对象赋值给firstchess,用x1,y1记录坐标<br/>
             *      如果选中了棋子，判断是否为敌方棋子<br/>
             *     1.如果是提示“点成敌方棋子了”<br/>
             *     2.如果不是，将firstClick改为false}</p>
             *<p>（2）如果是第二次点击{<br/>
             *      由点击处的像素坐标转换为棋盘坐标，并将该坐标上的棋子对象赋值给secondchess,用x1,y1记录坐标<br/>
             *      如果第二次选择的棋子是自己的，将该棋子对象重新赋值给firstClick(重新选中棋子)<br/>
             *      如果第二次没有选中棋子，判断是否可以走棋{<br/>
             *     1. 如果isAbleToMove(firstChess, x2, y2))返回true，说明可以走棋，变更棋盘和棋子信息，并进行记录<br/>
             *      发送倒置后的棋子信息给对方，重置isFirstClick为true，<br/>
             *     2.否则说明不符合走棋规则，修改信息为“不符合走棋规则”<br/>
             *     3.如果选中的是对方棋子，判断是否可以走棋<br/>
             *          a.如果isAbleToMove(firstChess, x2, y2))返回true，说明可以吃棋，变更棋盘和棋子信息，并进行记录<br/>
             *          发送倒置后的棋子信息给对方，重置isFirstClick为true，同时判断吃掉的是不是"帅"或者"将"<br/>
             *          如果是发送输赢信息并结束比赛，将isMyTurn改为false}<br/>
             *          b.否则说明不能吃棋，修改信息为”不能吃棋“}</p>
             */
            private void selectedchess(MouseEvent e) {
                int index1, index2;
                if (isFirstClick) {
                    //e.getX()，e.getY()得到鼠标点击的坐标
                    firstChess = analyse(e.getX(), e.getY());
                    x1 = tempX;
                    y1 = tempY;
                    if (firstChess != null) {
                        if (firstChess.player != localPlayer) {
                            message = "点成对方棋子了";
                            return;
                        }
                        isFirstClick = false;
                    }
                } else {
                    secondChess = analyse(e.getX(), e.getY());
                    x2 = tempX;
                    y2 = tempY;
                    if (secondChess != null) {
                        //如果第二次选中了棋子
                        if (secondChess.player == localPlayer) {
                            //第二次选中了我方棋子，对第一次点击的棋子进行更换
                            firstChess = secondChess;
                            x1 = tempX;
                            y1 = tempY;
                            secondChess = null;
                            return;
                        }
                    }
                    if (secondChess == null) {
                        //点击空处，判断是否可以走棋
                        if (isAbleToMove(firstChess, x2, y2)) {
                            index1 = map[x1][y1];
                            map[x1][y1] = -1;
                            map[x2][y2] = index1;
                            chess[index1].setPoint(x2, y2);
                            send("move" + "|" + index1 + "|" + (9 - x2) + "|" + (8 - y2) + "|" + (9 - x1) + "|" + (8 - y1) + "|" + "-1");
                            //存储我方下棋信息
                            list.add(new node(index1, x2, y2, x1, y1, -1));
                            isFirstClick = true;
                            repaint();
                            setMyTurn(false);
                        } else {
                            message = "不符合走棋规则";
                        }
                        return;
                    }
                    //是否能吃棋
                    if (secondChess != null && isAbleToMove(firstChess, x2, y2)) {
                        isFirstClick = true;
                        index1 = map[x1][y1];
                        index2 = map[x2][y2];
                        map[x1][y1] = -1;
                        map[x2][y2] = index1;
                        chess[index1].setPoint(x2, y2);
                        chess[index2] = null;
                        repaint();
                        send("move" + "|" + index1 + "|" + (9 - x2) + "|" + (8 - y2) + "|" + (9 - x1) + "|" + (8 - y1) + "|" + index2 + "|");
                        list.add(new node(index1, x2, y2, x1, y1, index2));
                        if (index2 == 0) {
                            //将被吃掉
                            message = "红方赢了";
                            JOptionPane.showConfirmDialog(null, "红方赢了", "提示", JOptionPane.DEFAULT_OPTION);
                            send("succ" + "|" + "红方赢了" + "|");
                            return;
                        }
                        if (index2 == 16) {
                            //帅被吃掉
                            message = "黑方赢了";
                            JOptionPane.showConfirmDialog(null, "黑方赢了", "提示", JOptionPane.DEFAULT_OPTION);
                            send("succ" + "|" + "黑方赢了" + "|");
                            return;
                        }
                        setMyTurn(false);
                    } else {
                        message = "不能吃子";
                    }
                }
            }

            /**
             * {@code anaysle(int x,int y)}分析是否点击了某坐标<br/>
             * 通过点击的像素坐标和棋盘坐标的小矩形进行匹配，如果点位于矩形内，说明点击了该坐标.
             */
            private chess analyse(int x, int y) {
                int leftX = 28;
                int leftY = 20;
                int indexX = -1;
                int indexY = -1;
                for (int i = 0; i <= 9; i++) {
                    for (int j = 0; j <=8; j++) {
                        Rectangle r = new Rectangle(leftX + j * 62, leftY + i * 58, 40, 40);
                        if (r.contains(x, y)) {
                            indexX = i;
                            indexY = j;
                            break;
                        }
                    }
                }
                tempX = indexX;
                tempY = indexY;
                if (indexX == -1 && indexY == -1) {
                    return null;
                }
                if (map[indexX][indexY] == -1) {
                    return null;
                } else {
                    return chess[map[indexX][indexY]];
                }
            }
        }
        //鼠标监听结束
        );
    }

    /**
     * {@code isMyChess(int index)}判断是否是自己的棋子
     */
    private boolean isMyChess(int index) {
        //棋子下标在0-15之间且当前执子方为黑方返回真
        if (index >= 0 && index <= 15 && localPlayer == BLACK_PLAYER) {
            return true;
        }
        //棋子下标在16-31之间且当前执子方为红方返回真
        if (index >= 16 && index <= 31 && localPlayer == RED_PLAYER) {
            return true;
        }
        return false;
    }

    /**
     *{@code setMyTurn(boolean b)} 设定是否是自己的回合
     */
    private void setMyTurn(boolean b) {
        isMyTurn = b;
        if (b) {
            message = "请您开始走棋";
        } else {
            message = "对方正在思考";
        }
    }

    /**
     * {@code rebackChess(int index, int x, int y, int oldX, int oldY)} 将棋子回退到上一步，<br/>
     * 并清空棋子未退回前的棋盘位置信息
     */
    private void rebackChess(int index, int x, int y, int oldX, int oldY) {
        chess[index].setPoint(oldX, oldY);
        map[oldX][oldY] = index;
        map[x][y] = -1;
    }

    /**
     * {@code resetChess(int index, int x, int y) }将吃掉的一个棋子重新放回棋盘
     */
    private void resetChess(int index, int x, int y) {
        int temp = index < 16 ? BLACK_PLAYER : RED_PLAYER;
        String name = null;
        switch (index) {
            case 0:
                name = "将";
                break;
            case 1:
            case 2:
                name = "士";
                break;
            case 3:
            case 4:
                name = "象";
                break;
            case 5:
            case 6:
                name = "马";
                break;
            case 7:
            case 8:
                name = "车";
                break;
            case 9:
            case 10:
                name = "炮";
                break;
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                name = "卒";
                break;
            case 16:
                name = "帅";
                break;
            case 17:
            case 18:
                name = "仕";
                break;
            case 19:
            case 20:
                name = "相";
                break;
            case 21:
            case 22:
                name = "马";
                break;
            case 23:
            case 24:
                name = "车";
                break;
            case 25:
            case 26:
                name = "炮";
                break;
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
                name = "兵";
                break;
            default:
                break;
        }
        chess[index] = new chess(temp, name, x, y);
        map[x][y] = index;
    }

    /**
     * {@code startJoin(String ip, int otherPort, int myPort) }输入需要联机的ip，2台电脑的端口，开始加入
     */
     public void startJoin(String ip, int otherPort, int myPort) {
        flag = true;
        this.otherPort = otherPort;
        this.myPort = myPort;
        this.ip = ip;
        System.out.println("能帮我链接到" + ip + "吗？");
        send("join|");
        //用线程池创建线程
        //  corePoolSize： 核心池的大小, 或者说线程池维护线程的最少数量
        //  maximumPoolSize： 线程池中线程的最大数量
        //  keepAliveTime： 线程池维护线程所允许的空闲时间
        //  unit： 线程池维护线程所允许的空闲时间的单位
        //  workQueue： 线程池所使用的缓冲队列
        //  handler： 线程池对拒绝任务的处理策略

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                3,6,2, TimeUnit.SECONDS ,
                new ArrayBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.AbortPolicy()
        );
         threadPoolExecutor.execute(this);

        //Thread th = new Thread((this));
        // th.start();
    }

    /**
     * 联机成功后，{@code tartNewGame(int player)}根据玩家执子颜色调用initMap(),initChess()初始化棋子布局<br/>
     * 布局为下红上黑，若玩家执黑子，则调用reverseBoard（）对棋子位子进行对调，变成下黑上红<br/>
     * 布局后将所有棋子和棋盘重画显示
     */
    public void startNewGame(int player) {
        initMap();
        initChess();
        if (player == BLACK_PLAYER) {
            reverseBoard();
        }
        repaint();
    }

    /**
     * {@code initChess()}初始化棋子布局
     */
    private void initChess() {
        //黑方棋子
        chess[0] = new chess(BLACK_PLAYER, "将", 0, 4);
        map[0][4] = 0;
        chess[1] = new chess(BLACK_PLAYER, "士", 0, 3);
        map[0][3] = 1;
        chess[2] = new chess(BLACK_PLAYER, "士", 0, 5);
        map[0][5] = 2;
        chess[3] = new chess(BLACK_PLAYER, "象", 0, 2);
        map[0][2] = 3;
        chess[4] = new chess(BLACK_PLAYER, "象", 0, 6);
        map[0][6] = 4;
        chess[5] = new chess(BLACK_PLAYER, "马", 0, 1);
        map[0][1] = 5;
        chess[6] = new chess(BLACK_PLAYER, "马", 0, 7);
        map[0][7] = 6;
        chess[7] = new chess(BLACK_PLAYER, "车", 0, 0);
        map[0][0] = 7;
        chess[8] = new chess(BLACK_PLAYER, "车", 0, 8);
        map[0][8] = 8;
        chess[9] = new chess(BLACK_PLAYER, "炮", 2, 1);
        map[2][1] = 9;
        chess[10] = new chess(BLACK_PLAYER, "炮", 2, 7);
        map[2][7] = 10;
        for (int i = 0; i < 5; i++) {
            chess[11 + i] = new chess(BLACK_PLAYER, "卒", 3, i * 2);
            map[3][i * 2] = 11 + i;
        }
        //红方棋子
        chess[16] = new chess(RED_PLAYER, "帅", 9, 4);
        map[9][4] = 16;
        chess[17] = new chess(RED_PLAYER, "仕", 9, 3);
        map[9][3] = 17;
        chess[18] = new chess(RED_PLAYER, "仕", 9, 5);
        map[9][5] = 18;
        chess[19] = new chess(RED_PLAYER, "相", 9, 2);
        map[9][2] = 19;
        chess[20] = new chess(RED_PLAYER, "相", 9, 6);
        map[9][6] = 20;
        chess[21] = new chess(RED_PLAYER, "马", 9, 1);
        map[9][1] = 21;
        chess[22] = new chess(RED_PLAYER, "马", 9, 7);
        map[9][7] = 22;
        chess[23] = new chess(RED_PLAYER, "车", 9, 0);
        map[9][0] = 23;
        chess[24] = new chess(RED_PLAYER, "车", 9, 8);
        map[9][8] = 24;
        chess[25] = new chess(RED_PLAYER, "炮", 7, 1);
        map[7][1] = 25;
        chess[26] = new chess(RED_PLAYER, "炮", 7, 7);
        map[7][7] = 26;
        for (int i = 0; i < 5; i++) {
            chess[27 + i] = new chess(RED_PLAYER, "兵", 6, i * 2);
            map[6][i * 2] = 27 + i;
        }
    }

    /**
     * {@code reverseBoard()}翻转棋局
     */
    private void reverseBoard() {
        for (int i = 0; i < 32; i++) {
            if (chess[i] != null) {
                chess[i].reversePoints();
            }
        }
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 9; j++) {
                int temp = map[i][j];
                map[i][j] = map[9 - i][8 - j];
                map[9 - i][8 - j] = temp;
            }
        }
    }

    /**
     *{@code  paint(Graphics g)} 重画游戏中的背景棋盘和棋子对象以及提示消息
     */

    @Override
    public void paint(Graphics g) {
        g.clearRect(0, 0, this.getWidth(), this.getHeight());
        Image backGroundImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chessBoard.png");
        g.drawImage(backGroundImage, 0, 0, 600, 600, this);
        for (int i = 0; i < 32; i++) {
            if (chess[i] != null) {
                chess[i].paint(g, this);
            }
        }
        if (firstChess != null) {
            firstChess.drawSelectedChess(g);
        }
        if (secondChess != null) {
            secondChess.drawSelectedChess(g);
        }
        g.drawString(message, 0, 620);
    }

    /**
     * {@code  isAbleToMove(chess firstChess, int x, int y)}判断各棋子落子是否正确，可以落子返回真，不能落子返回假.
     */
    private boolean isAbleToMove(chess firstChess, int x, int y) {
        int oldX, oldY;
        oldX = firstChess.x;
        oldY = firstChess.y;
        String chessName = firstChess.typeName;
        //判断将/帅走棋是否正确
        //判断是否”将“见面
        if ("将".equals(chessName) || "帅".equals(chessName)) {
            if (oldY == y && (map[x][y] == 0 || map[x][y] == 16)) {
                for (int i = x + 1; i < oldX; i++) {
                    if (map[i][y] != -1) {
                        return false;
                    }
                }
                return true;
            }
            //如果斜着走
            if ((x - oldX) * (y - oldY) != 0) {
                return false;
            }
            //如果直走超过一格
            if (Math.abs(x - oldX) > 1 || Math.abs(y - oldY) > 1) {
                return false;
            }
            //如果超出九宫格
            if ((x > 2 && x < 7) || y > 5 || y < 3) {
                return false;
            }
            return true;
        }
        //判断士/仕走棋是否正确
        if ("士".equals(chessName) || "仕".equals(chessName)) {
            //如果士/仕直走
            if ((x - oldX) * (y - oldY) == 0) {
                return false;
            }
            //如果士/仕斜走超过一格
            if (Math.abs(x - oldX) > 1 || Math.abs(y - oldY) > 1) {
                return false;
            }
            // 如果士/仕超出九宫格
            if ((x > 2 && x < 7) || y > 5 || y < 3) {
                return false;
            }
            return true;
        }
        //判断象/相走棋是否正确
        //此处不能从第7行返回初始位置，可能代码逻辑有错误！！！！！！！！！！！！！！！！！！！！！！！！！
        //一个未修正的错误，请改正
        if ("象".equals(chessName) || "相".equals(chessName)) {
            // 如果象/相直走
            if ((x - oldX) * (y - oldY) == 0) {
                return false;
            }
            // 如果象/相横向或者纵向的位移不等于2
            if (Math.abs(x - oldX) != 2 || Math.abs(y - oldY) != 2) {
                return false;
            }
            //如果象/相超出楚河汉界
            if (x < 5) {
                return false;
            }
            //如果象/相眼被堵
            //象/相左上跳
            int i = 0, j = 0;
            if (x - oldX == -2 && y - oldY == -2) {
                i = oldX - 1;
                j = oldY - 1;
            }
            //象/相右上跳

            if (x - oldX == -2 && y - oldY == 2) {
                i = oldX - 1;
                j = oldY + 1;
            }
            //象/相左下跳
            if (x - oldX == 2 && y - oldY == 2) {
                i = oldX + 1;
                j = oldY - 1;
            }
            //象/相右下跳
            if (x - oldX == 2 && y - oldY == 2) {
                i = oldX + 1;
                j = oldY + 1;
            }
            if (map[i][j] != -1) {
                return false;
            }
            return true;
        }
        //判断马走棋是否正确
        if ("马".equals(chessName)) {
            //如果马不走日字(横向位移*纵向位移不等于2)
            if (Math.abs(x - oldX) * Math.abs(y - oldY) != 2) {
                return false;
            }
            //如果马向上跳，且横向位移等于纵向位移*2
            if (x - oldX == -2) {
                if (map[oldX - 1][oldY] != -1) {
                    return false;
                }
            }
            //如果马向下跳，且横向位移等于纵向位移*2
            if (x - oldX == 2) {
                if (map[oldX + 1][oldY] != -1) {
                    return false;
                }
            }
            // 如果马向左跳，且纵向位移等于横向位移*2
            if (y - oldY == -2) {
                if (map[oldX][oldY - 1] != -1) {
                    return false;
                }
            }
            //如果马向右跳，且纵向位移等于横向位移*2
            if (y - oldY == 2) {
                if (map[oldX][oldY + 1] != -1) {
                    return false;
                }
            }
            return true;
        }
        // 判断车走棋是否正确
        if ("车".equals(chessName)) {
            //如果车斜走
            if (x - oldX != 0 && y - oldY != 0) {
                return false;
            }
            //如果车横向移动，且中间有棋子
            //判断过程优化
            if (x != oldX) {
                if (oldX > x) {
                    int temp = x;
                    x = oldX;
                    oldX = temp;
                }

                for (int i = oldX + 1; i < x; i++) {
                    if (map[i][oldY] != -1) {
                        return false;
                    }
                }
            }
            //如果车纵向移动，且中间有棋子
            //判断过程优化
            if (y != oldY) {
                if (oldY > y) {
                    int temp = y;
                    y = oldY;
                    oldY = temp;
                }
                for (int i = oldY + 1; i < y; i++) {
                    if (map[oldX][i] != -1) {
                        return false;
                    }
                }
            }
            return true;
        }
        //判断炮走棋是否正确
        if ("炮".equals(chessName)) {
            //记录纵向棋子是否交换过
            boolean swapFlagX = false;
            //记录横向棋子是否交换过
            boolean swapFlagY = false;
            //如果炮斜走
            if ((x - oldX) * (y - oldY) != 0) {
                return false;
            }
            //n记录两子之间有多少棋子
            int n = 0;
            //如果炮纵向运动
            if (x != oldX) {
                //判断过程优化
                if (oldX > x) {
                    int temp = x;
                    x = oldX;
                    oldX = temp;
                    swapFlagX = true;
                }
                for (int i = oldX + 1; i < x; i++) {
                    if (map[i][oldY] != -1) {
                        n += 1;
                    }
                }
            }
            //如果炮横向运动
            if (y != oldY) {
                //判断过程优化
                if (oldY > x) {
                    int temp = y;
                    y = oldY;
                    oldY = temp;
                    swapFlagY = true;
                }
                for (int i = oldY + 1; i < y; i++) {
                    if (map[oldX][i] != -1) {
                        n += 1;
                    }
                }
            }
            //中间超过一个子
            if (n > 1) {
                return false;
            }
            //中间没有子
            //如果之前交换过，则交换回来
            if (n == 0) {
                if (swapFlagX) {
                    int temp = x;
                    x = oldX;
                    oldX = temp;
                }
                if (swapFlagY) {
                    int temp = y;
                    y = oldY;
                    oldY = temp;
                }
                //如果目标处有棋子，则不能移动
                if (map[x][y] != -1) {
                    return false;
                }
            }
            //如果中间只有一个子
            if (n == 1) {
                //如果之前交换过，则交换回来
                if (swapFlagX) {
                    int temp = x;
                    x = oldX;
                    oldX = temp;
                }
                if (swapFlagY) {
                    int temp = y;
                    y = oldY;
                    oldY = temp;
                }
                //如果目标处没有棋子，则不能移动
                if (map[x][y] == -1) {
                    return false;
                }
            }

            return true;
        }
        //判断兵/卒走棋是否正确
        if ("卒".equals(chessName) || "兵".equals(chessName)) {
            //如果兵/卒斜走
            if ((x - oldX) * (y - oldY) != 0) {
                return false;
            }
            //如果移动超过一格
            if (Math.abs(x - oldX) > 1 || Math.abs(y - oldY) > 1) {
                return false;
            }
            //如果兵/卒未过河，只能向上移动
            if (oldX >= 5) {
                if (Math.abs(y - oldY) > 0) {
                    return false;
                }
                if (x - oldX == 1) {
                    return false;
                }
            } else {
                //如果已经过河，则不能向下移动
                if (x - oldX == 1) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }


    /**
     *{@code  send(String str)}发送消息
     */
    public void send(String str) {
        DatagramSocket s = null;
        try {
            s = new DatagramSocket();
            //一个缓冲区
            byte[] buffer;
            buffer = new String(str).getBytes();
            InetAddress ia = InetAddress.getByName(java.lang.String.valueOf(ip));
            System.out.println("请求连接的ip是" + ip);
            DatagramPacket dgp = new DatagramPacket(buffer, buffer.length, ia, myPort);
            s.send((dgp));
            System.out.println("发送信息:" + str);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (s != null) {
                s.close();
            }
        }
    }
    /**
     *{@code  run()}重写run()
     */
    @Override
    public void run() {
        try {
            System.out.println("我是客户端，我绑定的端口是" +myPort);
            //UDP协议
            // 创建数据报套接字与服务端进行通信连接，并发送和接受数据报套接字。
            DatagramSocket s = new DatagramSocket(myPort);
            byte[] data = new byte[100];
            //dgp包含了一个对保存自寻址数据报信息的字节数组的引用，length表示字节数组的长度
            DatagramPacket dgp = new DatagramPacket(data, data.length);
            while (flag) {
                //接收数据报
                s.receive(dgp);
                String strData = new String(data);
                String[] array = new String[6];
                //split（）根据匹配给定的正则表达式来拆分字符串，“|”为转义字符，使用时要加\\
                array = strData.split("\\|");
                //如果对局被加入，我是黑方
                if ("join".equals(array[0])) {
                    localPlayer = BLACK_PLAYER;
                    startNewGame(localPlayer);
                    if (localPlayer == RED_PLAYER) {
                        setMyTurn(true);
                    } else {
                        setMyTurn(false);
                    }
                    //发送联机成功消息
                    send("conn|");
                } else if ("conn".equals(array[0])) {
                    //我加入别人的战局，我是红方
                        localPlayer = RED_PLAYER;
                        startNewGame(localPlayer);
                        if (localPlayer == BLACK_PLAYER) {
                            setMyTurn(true);
                        } else {
                            setMyTurn(false);
                        }
                    } else if ("succ".equals(array[0])) {
                        if ("黑方赢了".equals(array[1])) {
                            if (localPlayer == RED_PLAYER) {
                                //showConfirmDialog()
                                // 带有自定义选择按钮的选择提示框，按钮和提示消息均可自定义
                                JOptionPane.showConfirmDialog(null, "黑方赢了，你可以重新开始", "你输了", JOptionPane.DEFAULT_OPTION);
                            } else {
                                JOptionPane.showConfirmDialog(null, "黑方赢了，你可以重新开始", "你赢了", JOptionPane.DEFAULT_OPTION);
                            }
                        }
                        if ("红方赢了".equals(array[1])) {
                            if (localPlayer == RED_PLAYER) {
                                JOptionPane.showConfirmDialog(null, "红方赢了，你可以重新开始", "你赢了", JOptionPane.DEFAULT_OPTION);
                            } else {
                                JOptionPane.showConfirmDialog(null, "红方赢了，你可以重新开始", "你输了", JOptionPane.DEFAULT_OPTION);
                            }
                        }
                        message = "你可以重新开局";
                        //可以点击开始按钮
                        gameclient.buttonStart.setEnabled(true);
                    } else if ("move".equals(array[1])) {
                            //对方的走棋信息
                            System.out.println("接受信息" + array[0] + "|" + array[1] + "|" + array[2] + "|" + array[3] + "|" + array[4] + "|" + array[5] + "|" + array[6] + "|");
                            int index = Integer.parseInt(array[1]);
                            x2 = Integer.parseInt(array[2]);
                            y2 = Integer.parseInt(array[3]);


                            int oldX = Integer.parseInt(array[4]);
                            int oldY = Integer.parseInt(array[5]);
                            int eatChessIndex = Integer.parseInt(array[6]);
                            list.add(new node(index, x2, y2, oldX, oldY, eatChessIndex));
                            message = "对方将棋子\"" + chess[index].typeName + "\"移动到了(" + x2 + "," + y2 + ")\n现在该你走棋";
                            //将棋子从x1y1，要移动到x2y2。
                            chess c = chess[index];
                            x1 = c.x;
                            y1 = c.y;
                            index = map[x1][y1];
                            int index2 = map[x2][y2];
                            map[x1][y1] = -1;
                            map[x2][y2] = index;
                            chess[index].setPoint(x2, y2);
                            //移动完成后，将索引index2数据清空

                            if (index2 != -1) {
                                chess[index2] = null;
                            }
                            repaint();
                            isMyTurn = true;
                    } else if ("quit".equals(array[0])) {
                            JOptionPane.showConfirmDialog(null, "对方退出了，游戏结束！", "提示", JOptionPane.DEFAULT_OPTION);
                            message = "对方退出了，游戏结束！";
                            gameclient.buttonStart.setEnabled(true);
                    } else if ("lose".equals(array[0])) {
                            JOptionPane.showConfirmDialog(null, "恭喜你，对方认输了！", "你赢了", JOptionPane.DEFAULT_OPTION);
                            setMyTurn(false);
                            gameclient.buttonStart.setEnabled(true);
                    } else if ("ask".equals(array[0])) {
                            String msg = "对方请求悔棋，是否同意？";
                            //yes或者no选择框
                            int type = JOptionPane.YES_NO_OPTION;
                            String title = "请求悔棋";
                            int choice = 0;
                            //返回0或者1
                            choice = JOptionPane.showConfirmDialog(null, msg, title, type);
                            if (choice == 1) {
                                send("refuse| ");
                            } else if (choice == 0) {
                                send("agree|");
                                message = "同意对方悔棋，对方正在思考";
                                setMyTurn(false);

                                //获取list容器最后一个元素的值
                                node temp = list.get(list.size() - 1);
                                //删除list容器最后一个元素的值
                                list.remove(list.size() - 1);
                                //如果我是红方
                                if (localPlayer == RED_PLAYER) {

                                    if (temp.index >= 16) {
                                        //上一步是我下的，需要回退2步
                                        rebackChess(temp.index, temp.x, temp.y, temp.oldX, temp.oldY);
                                        if (temp.eatChessIndex != -1) {
                                            //上一步吃了子,将被吃子重新放回棋盘
                                            resetChess(temp.eatChessIndex, temp.x, temp.y);
                                        }
                                        temp = list.get(list.size() - 1);
                                        list.remove(list.size() - 1);
                                        //上一步是对方下的，需要回退1步
                                        rebackChess(temp.eatChessIndex, temp.x, temp.y, temp.oldX, temp.oldY);
                                        if (temp.eatChessIndex != -1) {
                                            resetChess(temp.eatChessIndex, temp.x, temp.y);
                                        }
                                    } else {
                                        //上一步是对方下的，需要回退1步
                                        rebackChess(temp.eatChessIndex, temp.x, temp.y, temp.oldX, temp.oldY);
                                        if (temp.eatChessIndex != -1) {
                                            resetChess(temp.eatChessIndex, temp.x, temp.y);
                                        }

                                    }

                                } else {
                                    //如果我是黑方
                                    if (temp.index < 16) {
                                        //上一步是我下的，需要回退2步
                                        rebackChess(temp.index, temp.x, temp.y, temp.oldX, temp.oldY);
                                        if (temp.eatChessIndex != -1) {
                                            resetChess(temp.eatChessIndex, temp.x, temp.y);
                                        }
                                        temp = list.get(list.size() - 1);
                                        list.remove(list.size() - 1);
                                        //上一步是对方下的，需要回退1步
                                        rebackChess(temp.eatChessIndex, temp.x, temp.y, temp.oldX, temp.oldY);
                                        if (temp.eatChessIndex != -1) {
                                            //上一步吃了子,将被吃子重新放回棋盘
                                            resetChess(temp.eatChessIndex, temp.x, temp.y);
                                        }

                                    } else {
                                            //上一步是对方下的，需要回退1步
                                            rebackChess(temp.eatChessIndex, temp.x, temp.y, temp.oldX, temp.oldY);
                                            if (temp.eatChessIndex != -1) {
                                                resetChess(temp.eatChessIndex, temp.x, temp.y);
                                            }

                                    }

                                }

                                repaint();
                            }

                    } else if ("agree".equals(array[0])) {
                                //对方同意悔棋
                        JOptionPane.showConfirmDialog(null, "对方同意了你的悔棋要求");
                        node temp = list.get(list.size() - 1);
                        list.remove(list.size() - 1);
                        if (localPlayer == RED_PLAYER) {
                            if (temp.index >= 16) {
                                //上一步是我下的，需要回退1步
                                rebackChess(temp.index, temp.x, temp.y, temp.oldX, temp.oldY);
                                if (temp.eatChessIndex != -1) {
                                    resetChess(temp.eatChessIndex, temp.x, temp.y);
                                }
                            } else {
                                //上一步是对方下的，需要回退2步
                                rebackChess(temp.eatChessIndex, temp.x, temp.y, temp.oldX, temp.oldY);
                                if (temp.eatChessIndex != -1) {
                                    resetChess(temp.eatChessIndex, temp.x, temp.y);
                                }
                                temp = list.get(list.size() - 1);
                                list.remove(list.size() - 1);

                                rebackChess(temp.eatChessIndex, temp.x, temp.y, temp.oldX, temp.oldY);
                                if (temp.eatChessIndex != -1) {
                                    resetChess(temp.eatChessIndex, temp.x, temp.y);
                                }

                            }

                        } else {
                            //如果我是黑方
                            if (temp.index < 16) {
                                //上一步是我下的，需要回退1步
                                rebackChess(temp.index, temp.x, temp.y, temp.oldX, temp.oldY);
                                if (temp.eatChessIndex != -1) {
                                    resetChess(temp.eatChessIndex, temp.x, temp.y);
                                }
                            } else {
                                //上一步是对方下的，需要回退2步
                                rebackChess(temp.eatChessIndex, temp.x, temp.y, temp.oldX, temp.oldY);
                                if (temp.eatChessIndex != -1) {
                                    resetChess(temp.eatChessIndex, temp.x, temp.y);
                                }

                                temp = list.get(list.size() - 1);
                                list.remove(list.size() - 1);

                                rebackChess(temp.eatChessIndex, temp.x, temp.y, temp.oldX, temp.oldY);
                                if (temp.eatChessIndex != -1) {
                                    //上一步吃了子,将被吃子重新放回棋盘
                                    resetChess(temp.eatChessIndex, temp.x, temp.y);

                                }
                            }

                        }
                        setMyTurn(true);
                        repaint();
                     } else if ("refuse".equals(array[0])) {
                    JOptionPane.showConfirmDialog(null, "对方拒绝了你的悔棋要求");
                    }

            }

        }
       /** 当try语句中出现异常时，会执行catch中的语句，
        * java运行时系统会自动将catch括号中的Exception e 初始化，
        * 也就是实例化Exception类型的对象。e是此对象引用名称。
        * 然后e（引用）会自动调用Exception类中指定的方法，也就出现了e.printStackTrace() ;。
        * printStackTrace()方法的意思是：在命令行打印异常信息在程序中出错的位置及原因。
        */

        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package chinese_chess;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JPanel;

/**
 * @author li+,
 * @since 2020-7-25
 * @version 3.0
 */

/**
 * {@code chess} 棋子类：包括玩家、棋子名、棋盘对应二维数组的下标.<br/>
 * RED_PLAYER   红方玩家.<br/>
 * BLACK_PLAYER 黑方玩家.<br/>
 * typeName;    棋子名称.<br/>
 * x,y;         棋子的坐标.<br/>
 * chessImage;  棋子所用的图.<br/>
 * leftX=30,leftY=35;   水平边缘偏移和垂直边缘偏移.<br/>
 */

public class chess {
    public static final  int RED_PLAYER = 1;
    public static final  int BLACK_PLAYER = 0;
    public int player;
    public String typeName;
    public int x,y;
    private Image chessImage;
    private int leftX=28,leftY=20;
/**
 * {@code chess} 棋子的构造方法.<br/>
 *  @param player 玩家（1为红方，0为黑方）.<br/>
 *  @param typeName 棋子名（"将"、"帅"......）.<br/>
 *  @param  x  （棋盘对应二维数组的下标之x，0~9）.<br/>
 *  @param  y  （棋盘对应二维数组的下标之y，0~8）.<br/>
 */
    public chess( int player,String typeName,int x,int y) {
        this.player = player;
        this.typeName = typeName;
        this.x = x;
        this.y = y;
        if (player == RED_PLAYER) {
            switch (typeName) {
                case "帅":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess7.png");
                    break;
                case "仕":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess8.png");
                    break;
                case "相":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess9.png");
                    break;
                case "马":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess10.png");
                    break;
                case "车":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess11.png");
                    break;
                case "炮":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess12.png");
                    break;
                case "兵":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess13.png");
                    break;
                default:
                    break;
            }
        } else {
            switch (typeName) {
                case "将":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess0.png");
                    break;
                case "士":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess1.png");
                    break;
                case "象":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess2.png");
                    break;
                case "马":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess3.png");
                    break;
                case "车":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess4.png");
                    break;
                case "炮":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess5.png");
                    break;
                case "卒":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess6.png");
                    break;
                default:
                    break;
            }
        }
    }
    /**
     * 函数{@code setPoint(int x, int y)}用来把棋子放到x,y坐标.<br/>
     *  @param x  棋盘上第x(0~9)条横线.<br/>
     *  @param y  棋盘上第y(0~8)条竖线.
      */
    public  void setPoint(int x, int y){
        this.x=x;
        this.y=y;
    }
    /**
     * 函数{@code reversePoints()}用来把棋子翻转
     */
    public void reversePoints(){
        x=9-x;
        y=8-y;
    }
    /**
     * 函数{@code paint(Graphics g,JPanel i)}在指定的Jpanel上绘制棋子.<br/>
     * @param  g  Graphics g一个抽象类，就像一个画笔，为我们绘制各种图形.<br/>
     * @param  i JPanel i 一个容器类，可放置按钮，文本框等组件.<br/>
     * img - 要绘制的指定图像。如果 img 为 null，则此方法不执行任何动作.<br/>
     * x - x 坐标.<br/>
     * y - y 坐标.<br/>
     * width - 矩形的宽度.<br/>
     * height - 矩形的高度.<br/>
     * observer - 当转换了更多图像时要通知的对象.
     */
    protected void paint(Graphics g,JPanel i){

        g.drawImage(chessImage,leftX+y*62,leftY+x*58,40,40,i);
    }
    /**
     * 函数{@code drawSelectedChess(Graphics g)}用于绘制选择棋子时的矩形边框.<br/>
     * x - 要绘制矩形的 x 坐标.<br/>
     * y - 要绘制矩形的 y 坐标.<br/>
     * width - 要绘制矩形的宽度.<br/>
     * height - 要绘制矩形的高度.
     */
    public void drawSelectedChess(Graphics g)
    {

        g.drawRect(leftX+x*62,leftY+y*58,40,0);
    }
}
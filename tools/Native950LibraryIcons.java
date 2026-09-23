import com.rs.cache.Cache;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import java.nio.file.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Read-only contact sheet of the bank's authored custom-tab icons (enum15585). */
public final class Native950LibraryIcons {
    private static int u16(byte[] b,int p){return (b[p]&255)*256+(b[p+1]&255);}
    public static void main(String[] args)throws Exception{
        Cache.initFlatReadOnly(Paths.get("cache"));
        BufferedImage sheet=new BufferedImage(660,360,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=sheet.createGraphics();g.setColor(new Color(40,36,29));g.fillRect(0,0,660,360);
        for(int key=1;key<=33;key++){
            int id=RS3ClientScriptMap.getMap(15585).getIntValue(key);
            byte[] b=Cache.STORE.getIndexes()[8].getFile(id,0);int count=u16(b,b.length-2),meta=b.length-7-8*count;
            if(count!=1){
                int dw=u16(b,2),dh=u16(b,4),pixels=dw*dh;
                if(count!=32769||b[0]!=0||b[1]!=1||b.length!=8+pixels*4)throw new IllegalStateException("Unsupported direct sprite "+id);
                BufferedImage direct=new BufferedImage(dw,dh,BufferedImage.TYPE_INT_ARGB);
                for(int i=0;i<pixels;i++){int at=6+i*3;direct.setRGB(i%dw,i/dw,((b[6+pixels*3+i]&255)<<24)|((b[at]&255)<<16)|((b[at+1]&255)<<8)|(b[at+2]&255));}
                int cx=((key-1)%11)*60,cy=((key-1)/11)*120;g.drawImage(direct,cx+12,cy+15,36,36,null);g.setColor(Color.WHITE);g.drawString(key+" / "+id,cx,cy+74);continue;
            }
            int w=u16(b,meta),h=u16(b,meta+2),colors=(b[meta+4]&255)+1,x=u16(b,meta+5),y=u16(b,meta+7),sw=u16(b,meta+9),sh=u16(b,meta+11);
            int[] palette=new int[colors];int p=meta-3*(colors-1);
            for(int c=1;c<colors;c++){palette[c]=((b[p]&255)<<16)|((b[p+1]&255)<<8)|(b[p+2]&255);p+=3;}
            int flags=b[0]&255,size=sw*sh;
            if(flags>3||1+size*((flags&2)!=0?2:1)!=meta-3*(colors-1))throw new IllegalStateException("Unexpected sprite layout "+id);
            BufferedImage image=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
            for(int yy=0;yy<sh;yy++)for(int xx=0;xx<sw;xx++){
                int at=(flags&1)!=0?xx*sh+yy:yy*sw+xx,idx=b[1+at]&255,alpha=(flags&2)!=0?b[1+size+at]&255:idx==0?0:255;
                image.setRGB(x+xx,y+yy,(alpha<<24)|palette[idx]);
            }
            int column=(key-1)%11,row=(key-1)/11;
            g.drawImage(image,column*60+12,row*120+15,36,36,null);g.setColor(Color.WHITE);
            g.drawString(Integer.toString(key),column*60+22,row*120+74);g.drawString(Integer.toString(id),column*60+8,row*120+94);
        }
        g.dispose();ImageIO.write(sheet,"png",Paths.get(args[0]).toFile());
    }
}

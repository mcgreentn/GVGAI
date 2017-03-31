package ontology.effects.unary;

import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import core.content.InteractionContent;
import core.game.Game;
import core.logging.Logger;
import core.logging.Message;
import ontology.avatar.MovingAvatar;
import ontology.effects.Effect;

import java.util.Iterator;
import java.util.Objects;

public class RemoveScore extends Effect {

    //Indicates if the second sprite should be killed.
    public boolean killSecond = false;
    public String stype = "";
    public int itype = -1;

    public RemoveScore(InteractionContent cnt)
    {
        this.parseParameters(cnt);
        if (!Objects.equals(stype, "")) {
            itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
        }
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
	if(sprite1 == null){
            String[] className = this.getClass().getName().split(".");
            Logger.getInstance().addMessage(new Message(Message.WARNING, "[" + className[className.length - 1] + "] sprite1 is null."));
            return;
        }
	
        if (Objects.equals(stype, "")) {
            if (sprite1.is_avatar) {
                MovingAvatar a = (MovingAvatar) sprite1;
                a.setScore(0);
                if (killSecond && sprite2 != null)
                    game.killSprite(sprite2, true);
            }
        } else {
            Iterator<VGDLSprite> spriteIt = game.getSpriteGroup(itype);

            if(spriteIt != null) while(spriteIt.hasNext())
            {
                VGDLSprite s = spriteIt.next();
                if (s.is_avatar) {
                    MovingAvatar a = (MovingAvatar) s;
                    a.setScore(0);
                    if (killSecond && sprite2 != null)
                        game.killSprite(sprite2, true);
                }
            }
        }
    }

}

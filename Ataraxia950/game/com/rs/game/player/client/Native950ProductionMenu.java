package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.network.protocol.modern950.Native950Actions;
import java.util.*;
import java.util.function.IntConsumer;

/** Session-owned production callbacks, with the paired950 native Make-X presentation when mapped. */
public final class Native950ProductionMenu {
    public static final class Choice {
        public final String label;
        private final IntConsumer start;
        private final boolean quantity;
        private final Native950Production.Recipe recipe;
        public Choice(String label,IntConsumer start){this.label=Objects.requireNonNull(label);this.start=Objects.requireNonNull(start);this.quantity=true;this.recipe=null;}
        public Choice(String label,Runnable start){this.label=Objects.requireNonNull(label);this.start=n->start.run();this.quantity=false;this.recipe=null;}
        public Choice(Native950Production.Recipe recipe,IntConsumer start){this(recipe.productName()+" (level "+recipe.level+")",recipe,start);}
        public Choice(String label,Native950Production.Recipe recipe,IntConsumer start){this.label=Objects.requireNonNull(label);this.recipe=Objects.requireNonNull(recipe);this.start=Objects.requireNonNull(start);this.quantity=true;}
        public Native950Production.Recipe recipe(){return recipe;}
        public void start(int batches){start.accept(batches);}
        public boolean hasQuantity(){return quantity;}
    }
    private final Player player;
    private final Native950Dialogues dialogues;
    private final Native950ProductionInterface view;
    private List<Choice> choices=Collections.emptyList();
    private Choice selected;
    private int page;
    private String title;
    private Native950ProductionUiCatalog.Category category;
    private int root=-1,names=-1,display=-1,quantity,maximum;
    private boolean categoryPending;
    private Map<Integer,Choice> products=Collections.emptyMap();
    public Native950ProductionMenu(Player player,Native950Dialogues dialogues){
        this.player=player;this.dialogues=dialogues;view=new Native950ProductionInterface(player);
        player.getInterfaceManager().setNative950ProductionMenu(this);
    }
    public boolean isOpen(){return !choices.isEmpty();}
    public boolean isNativeOpen(){return view.isOpen();}
    public int currentCategoryId(){return category==null?-1:category.id;}
    public int selectedDisplayId(){return display;}
    public int selectedQuantity(){return quantity;}
    public int maximumQuantity(){return maximum;}
    public static void verifyCacheBindings(){Native950ProductionInterface.verify();}
    public void verifyBeforeOpen(){if(Cache.STORE!=null&&Cache.isFlatReadOnly())Native950ProductionInterface.verify();}
    public void open(String title,List<Native950Production.Recipe> recipes){
        List<Choice> list=new ArrayList<>();
        for(Native950Production.Recipe recipe:recipes)list.add(new Choice(recipe,amount->player.getActionManager().setAction(new Native950ProductionAction(recipe,amount))));
        openChoices(title,list);
    }
    public void openChoices(String title,List<Choice> list){
        if(list==null||list.isEmpty()){player.sendMessage("You do not have materials for that action.");return;}
        verifyBeforeOpen();close();this.choices=Collections.unmodifiableList(new ArrayList<>(list));this.title=title;page=0;selected=null;
        if(!openNative(choices))render();
    }
    /** Match output, skill and the cache's ingredient identities. Aliases such as shafts must keep their wood context. */
    private static int match(Choice choice,int display){
        Native950Production.Recipe recipe=choice.recipe;
        if(recipe==null||!choice.quantity||recipe.skill==Skills.SMITHING||recipe.produced().length==0)return -1;
        int output=recipe.produced()[0].getId();
        if(Native950ProductionUiCatalog.outputId(display)!=output)return -1;
        int skill=Native950ProductionUiCatalog.skill(display);if(skill>=0&&skill!=recipe.skill)return -1;
        ItemDefinitions definition=ItemDefinitions.getItemDefinitions(display);int matched=0;
        for(int i=0;i<10;i++){
            int material=definition.getCSOpcode(2655+i,-1);if(material<1)continue;
            boolean found=recipe.usesInput(material);for(int tool:recipe.tools())found|=tool==material;
            if(!found)return -1;matched++;
        }
        if(display!=output&&matched==0)return -1;
        return matched*10+(display==output?1:0);
    }
    private Map<Integer,Choice> mapped(Native950ProductionUiCatalog.Category candidate,List<Choice> offered){
        Map<Integer,Choice> result=new LinkedHashMap<>();
        for(int item:candidate.displayIds){Choice best=null;int score=-1;for(Choice choice:offered){int value=match(choice,item);if(value>score){score=value;best=choice;}}
            if(best!=null)result.put(item,best);
        }
        return result;
    }
    private boolean eligible(Choice choice){return choice.recipe!=null&&choice.quantity&&choice.recipe.skill!=Skills.SMITHING&&choice.recipe.produced().length>0;}
    private boolean openNative(List<Choice> offered){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())return false;
        for(Choice choice:offered)if(!eligible(choice))return false;
        Native950ProductionUiCatalog.Category best=null;int bestScore=-1,bestRoot=-1,bestNames=-1;
        Set<Integer> visited=new HashSet<>();
        for(Choice choice:offered)for(Native950ProductionUiCatalog.Category candidate:Native950ProductionUiCatalog.forProduct(choice.recipe.produced()[0].getId())){
            if(!visited.add(candidate.id))continue;
            Map<Integer,Choice> initial=mapped(candidate,offered);if(initial.isEmpty())continue;
            Set<Choice> covered=new HashSet<>(initial.values());int candidateRoot=-1,candidateNames=-1;
            if(covered.size()!=offered.size()&&candidate.rootId>=0&&candidate.namesId>=0){
                RS3ClientScriptMap menu=RS3ClientScriptMap.getMap(candidate.rootId);
                for(int i=0;i<menu.getSize();i++){Native950ProductionUiCatalog.Category other=Native950ProductionUiCatalog.byId(menu.getIntValue(i));if(other!=null)covered.addAll(mapped(other,offered).values());}
                candidateRoot=candidate.rootId;candidateNames=candidate.namesId;
            }
            if(covered.size()!=offered.size())continue;
            int score=initial.size()*100;
            for(Map.Entry<Integer,Choice> entry:initial.entrySet())score+=match(entry.getValue(),entry.getKey());
            if(score>bestScore){bestScore=score;best=candidate;bestRoot=candidateRoot;bestNames=candidateNames;}
        }
        if(best==null)return false;
        dialogues.close();category=best;root=bestRoot;names=bestNames;products=mapped(best,offered);
        display=products.keySet().iterator().next();selected=products.get(display);updateBound(true);
        view.open(root,names,category.id,display,maximum,quantity,title);
        return true;
    }
    private void updateBound(boolean all){
        maximum=selected==null||selected.recipe==null?0:Math.min(60,selected.recipe.maximumByMaterials(player));
        quantity=maximum==0?0:all?maximum:Math.max(1,Math.min(quantity,maximum));
    }
    private void refresh(){view.refresh(root,names,category.id,display,maximum,quantity,title);}
    private boolean blocked(){return player.isLocked()||player.closeInterfaceLocked||player.isDead()||player.getNextWorldTile()!=null||player.hasWalkSteps();}
    public boolean handle(Native950Actions.InterfaceAction action){
        int interfaceId=action.interfaceId();
        if(interfaceId==1477&&action.componentId()==896){
            if(!isNativeOpen())return false;
            if(blocked()){close();return true;}
            if(!categoryPending||action.option()!=1||action.itemId()!=-1)return true;
            categoryPending=false;view.closeDropdown();selectCategory(action.slot());return true;
        }
        if(interfaceId!=1370&&interfaceId!=1371)return false;
        // Consume stale Make-X packets even after cancellation; never hand them to a different owner.
        if(!isNativeOpen())return true;
        if(blocked()){close();return true;}
        if(action.option()!=1)return true;
        int component=action.componentId(),slot=action.slot(),item=action.itemId();
        if(interfaceId==1370){
            if(slot!=-1||item!=-1)return true;
            if(component==32){close();return true;}

            return true;
        }
        if(component==20){
            if(item!=-1||slot<0||slot>=60)return true;
            updateBound(false);if(slot>=maximum){refresh();return true;}
            quantity=slot+1;view.quantity(maximum,quantity);return true;
        }
        if(component==22){
            if(slot<1||(slot-1)%4!=0)return true;int ordinal=(slot-1)/4;
            if(ordinal>=category.displayIds.length)return true;int wanted=category.displayIds[ordinal];
            //The clickable overlay normally has no item; clients may include the alias/output identity.
            if(item!=-1&&item!=wanted&&item!=Native950ProductionUiCatalog.outputId(wanted))return true;
            Choice choice=products.get(wanted);
            if(choice==null){player.sendMessage("That product is not available from these materials or this work station yet.");refresh();return true;}
            display=wanted;selected=choice;updateBound(true);refresh();return true;
        }
        if(component==28&&item==-1&&slot==-1&&root>=0){
            categoryPending=true;view.openDropdown(RS3ClientScriptMap.getMap(root).getSize());
        }
        return true;
    }
    private void selectCategory(int slot){
        if(root<0||slot<0)return;
        RS3ClientScriptMap menu=RS3ClientScriptMap.getMap(root);if(slot>=menu.getSize())return;
        Native950ProductionUiCatalog.Category next=Native950ProductionUiCatalog.byId(menu.getIntValue(slot));
        Map<Integer,Choice> nextProducts=next==null?Collections.emptyMap():mapped(next,choices);
        if(nextProducts.isEmpty()){player.sendMessage("That category has no recipes available from this interaction yet.");refresh();return;}
        category=next;products=nextProducts;display=products.keySet().iterator().next();selected=products.get(display);updateBound(true);refresh();
    }
    private void make(){
        Choice choice=selected;int count=quantity;updateBound(false);
        String reason=choice==null?"Choose an item first.":choice.recipe.refusal(player);
        if(reason!=null){player.sendMessage(reason);refresh();return;}
        if(count<1||count>maximum){player.sendMessage("Your materials have changed. Choose the quantity again.");refresh();return;}
        close();choice.start(count);
    }
    private void render(){
        if(selected!=null){
            String all=selected.recipe==null?"Make all":"Make all ("+selected.recipe.maximumByMaterials(player)+" batches)";
            dialogues.options("How many batches?","Make 1","Make 5","Make 10",all,"Cancel");return;
        }
        List<String> rows=new ArrayList<>();int end=Math.min(page+3,choices.size());
        for(int i=page;i<end;i++)rows.add(choices.get(i).label);
        if(choices.size()>3)rows.add("More choices");rows.add("Cancel");dialogues.options(title,rows.toArray(new String[0]));
    }
    public boolean handle(Native950Actions.DialogueClickAction action){
        if(!isOpen())return false;
        if(isNativeOpen()){
            if(blocked()){close();return true;}
            //1370:30 has pauseText/optionMask1, so click and Space send dialogue opcode101.
            // It is not an IF_BUTTON. Old dialogue pages and forged dynamic slots cannot submit.
            if(action.interfaceId()==1370&&action.componentId()==30&&action.slot()==-1)make();
            return true;
        }
        if(blocked()){close();return true;}
        if(!dialogues.consumeResponse(action.interfaceId(),action.componentId(),action.slot()))return true;
        int row=(action.componentId()-8)/5;
        if(selected!=null){
            Choice choice=selected;close();int[] amounts={1,5,10,10000};if(row>=0&&row<amounts.length){
                int count=amounts[row];if(choice.recipe!=null){int available=choice.recipe.maximumByMaterials(player);if(available>0)count=Math.min(count,available);}
                choice.start(count);
            }
            return true;
        }
        int shown=Math.min(3,choices.size()-page);
        if(row>=0&&row<shown){selected=choices.get(page+row);if(selected.quantity){
            if(!openNative(Collections.singletonList(selected))){if(selected.recipe!=null)for(String line:selected.recipe.details(player))player.sendMessage(line);render();}
        }else{Choice choice=selected;close();choice.start(1);}}
        else if(row==shown&&choices.size()>3){page=page+3>=choices.size()?0:page+3;render();}else close();
        return true;
    }
    public void close(){
        boolean dialogue=isOpen()&&!view.isOpen();choices=Collections.emptyList();selected=null;page=0;
        products=Collections.emptyMap();category=null;categoryPending=false;root=names=display=-1;quantity=maximum=0;
        view.close();if(dialogue)dialogues.close();
    }
}

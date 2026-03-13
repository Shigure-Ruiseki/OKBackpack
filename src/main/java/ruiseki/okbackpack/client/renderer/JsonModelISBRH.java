package ruiseki.okbackpack.client.renderer;

import static com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.properties.ModelQuadFacing.VALUES;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizon.gtnhlib.blockstate.registry.BlockPropertyRegistry;
import com.gtnewhorizon.gtnhlib.client.model.ItemContext;
import com.gtnewhorizon.gtnhlib.client.model.ModelISBRH;
import com.gtnewhorizon.gtnhlib.client.model.baked.BakedModel;
import com.gtnewhorizon.gtnhlib.client.model.color.BlockColor;
import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.ModelQuadView;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.properties.ModelQuadFacing;
import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;

@ThreadSafeISBRH(perThread = true)
public class JsonModelISBRH extends ModelISBRH {

    public static final JsonModelISBRH INSTANCE = new JsonModelISBRH();

    private final ItemContext itemContext = new ItemContext();
    public final Random RAND = new Random();

    public JsonModelISBRH() {}

    public void renderToEntity(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (block == null) return;
        int meta = stack.getItemDamage();

        Tessellator tessellator = TessellatorManager.get();
        itemContext.stack = stack;
        itemContext.blockState = BlockPropertyRegistry.getBlockState(stack);
        itemContext.random = RAND;

        BakedModel model = getModel(itemContext);
        RenderHelpers.bindTexture(TextureMap.locationBlocksTexture);

        GL11.glPushMatrix();

        GL11.glRotated(90f, 0f, 1f, 0f);
        GL11.glRotatef(180f, 0f, 0f, 1f);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);

        tessellator.startDrawingQuads();

        int color = model.getColor(null, 0, 0, 0, block, meta, RAND);

        for (ModelQuadFacing dir : VALUES) {
            itemContext.quadFacing = dir;

            final var quads = model.getQuads(itemContext);
            if (quads.isEmpty()) continue;

            for (ModelQuadView quad : quads) {
                int quadColor = color;
                if (stack.getItem() != null && quad.getColorIndex() != -1) {
                    quadColor = BlockColor.getColor(block, stack, quad.getColorIndex());
                }

                float r = (quadColor & 0xFF) / 255f;
                float g = (quadColor >> 8 & 0xFF) / 255f;
                float b = (quadColor >> 16 & 0xFF) / 255f;

                final float shade = diffuseLight(quad.getComputedFaceNormal());
                tessellator.setColorOpaque_F(r * shade, g * shade, b * shade);
                renderQuad(quad, -0.5f, -0.5f, -0.5f, tessellator, null);
            }
        }

        tessellator.draw();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        itemContext.reset();
    }
}

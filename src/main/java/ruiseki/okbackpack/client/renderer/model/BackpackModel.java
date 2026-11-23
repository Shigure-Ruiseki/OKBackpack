
package ruiseki.okbackpack.client.renderer.model;

import static ruiseki.okbackpack.init.TierRegistries.LEATHER;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.gtnhlib.client.model.BakedModelQuadContext;
import com.gtnewhorizon.gtnhlib.client.model.JSONVariant;
import com.gtnewhorizon.gtnhlib.client.model.baked.BakedModel;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer.Position;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer.Position.ModelDisplay;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelRegistry;
import com.gtnewhorizon.gtnhlib.client.model.loading.ResourceLoc;
import com.gtnewhorizon.gtnhlib.client.model.unbaked.JSONModel;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.ModelQuadView;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.tier.BackpackTier;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okbackpack.backpack.BlockBackpack;
import ruiseki.okbackpack.mixins.early.gtnhlib.JSONModelAccessor;
import ruiseki.okcore.datastructure.ThreadsafeCache;
import ruiseki.okcore.helper.DirectionHelpers;

public class BackpackModel implements BakedModel {

    private static final ResourceLoc.ModelLoc MODEL_BASE = new ResourceLoc.ModelLoc(
        Reference.MOD_ID,
        "block/part/backpack_base");

    private static final ResourceLoc.ModelLoc MODEL_FRONT = new ResourceLoc.ModelLoc(
        Reference.MOD_ID,
        "block/part/backpack_front_pouch");

    private static final ResourceLoc.ModelLoc MODEL_LEFT = new ResourceLoc.ModelLoc(
        Reference.MOD_ID,
        "block/part/backpack_left_pouch");

    private static final ResourceLoc.ModelLoc MODEL_RIGHT = new ResourceLoc.ModelLoc(
        Reference.MOD_ID,
        "block/part/backpack_right_pouch");

    private record CacheKey(ResourceLoc.ModelLoc modelLoc, String tierId) {}

    private static final ThreadsafeCache<CacheKey, JSONModel> JSON_CACHE = new ThreadsafeCache<>(
        128,
        key -> loadAndPrepareJSON((CacheKey) key),
        false);

    public BackpackModel() {}

    @Override
    public List<ModelQuadView> getQuads(BakedModelQuadContext context) {
        List<ModelQuadView> quads = new ArrayList<>();

        ForgeDirection facing = ForgeDirection.SOUTH;
        BackpackTier tier = TierRegistry.getTier(LEATHER);

        if (context.getBlockState() != null) {
            facing = context.getBlockState()
                .getPropertyValue(BlockBackpack.DIRECTION_PROPERTY);

            tier = TierRegistry.getTier(
                context.getBlockState()
                    .getPropertyValue(BlockBackpack.TIER_PROPERTY));
        }

        addPartQuads(quads, MODEL_BASE, facing, context, tier);
        addPartQuads(quads, MODEL_FRONT, facing, context, tier);
        addPartQuads(quads, MODEL_LEFT, facing, context, tier);
        addPartQuads(quads, MODEL_RIGHT, facing, context, tier);

        return quads;
    }

    private void addPartQuads(List<ModelQuadView> target, ResourceLoc.ModelLoc modelLoc, ForgeDirection facing,
        BakedModelQuadContext context, BackpackTier tier) {
        try {
            JSONModel model = JSON_CACHE.get(new CacheKey(modelLoc, tier.getId()));
            if (model == null) return;

            BakedModel bakedModel = model.bake(createVariant(modelLoc, facing));
            if (bakedModel == null) return;

            List<ModelQuadView> quads = bakedModel.getQuads(context);
            if (quads != null) {
                target.addAll(quads);
            }
        } catch (Exception e) {
            // Ignore failed model part.
        }
    }

    private static JSONVariant createVariant(ResourceLoc.ModelLoc modelLoc, ForgeDirection facing) {
        return new JSONVariant(modelLoc, 0, DirectionHelpers.directionToRotationY(facing), false);
    }

    private static JSONModel loadAndPrepareJSON(CacheKey key) {
        JSONModel baseJson = ModelRegistry.getJSONModel(key.modelLoc());
        if (baseJson == null) return null;

        JSONModelAccessor accessor = (JSONModelAccessor) baseJson;

        var textures = new Object2ObjectOpenHashMap<>(baseJson.getTextures());

        textures.put(
            "#clips",
            TierRegistry.getTier(key.tierId())
                .getClipTexturePath()
                .toString());

        return new JSONModel(
            accessor.getParentId(),
            accessor.isUseAO(),
            accessor.getDisplay(),
            textures,
            accessor.getElements());
    }

    @Override
    public ModelDisplay getDisplay(Position pos, BakedModelQuadContext context) {
        JSONModel model = ModelRegistry.getJSONModel(MODEL_BASE);
        if (model != null) {
            return model.bake(createVariant(MODEL_BASE, ForgeDirection.SOUTH))
                .getDisplay(pos, context);
        }

        return ModelDisplay.DEFAULT;
    }

    @Override
    public IIcon getParticle(BakedModelQuadContext context) {
        JSONModel model = ModelRegistry.getJSONModel(MODEL_BASE);
        if (model != null) {
            return model.bake(createVariant(MODEL_BASE, ForgeDirection.SOUTH))
                .getParticle(context);
        }
        return null;
    }

}

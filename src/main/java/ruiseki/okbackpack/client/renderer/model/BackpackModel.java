
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

public class BackpackModel implements BakedModel {

    private static final ResourceLoc.ModelLoc MODEL = new ResourceLoc.ModelLoc(
        Reference.MOD_ID,
        "block/part/backpack_base");

    private record CacheKey(ResourceLoc.ModelLoc modelLoc, String tierId) {}

    private static final ThreadsafeCache<CacheKey, JSONModel> JSON_CACHE = new ThreadsafeCache<>(
        128,
        key -> loadAndPrepareJSON((CacheKey) key),
        false);

    public BackpackModel() {}

    @Override
    public List<ModelQuadView> getQuads(BakedModelQuadContext context) {
        ForgeDirection facing = ForgeDirection.SOUTH;
        BackpackTier tier = TierRegistry.getTier(LEATHER);

        if (context.getBlockState() != null) {
            facing = context.getBlockState()
                .getPropertyValue(BlockBackpack.DIRECTION_PROPERTY);
            tier = TierRegistry.getTier(
                context.getBlockState()
                    .getPropertyValue(BlockBackpack.TIER_PROPERTY));
        }

        JSONVariant variant = createVariant(facing);
        try {
            JSONModel model = JSON_CACHE.get(new CacheKey(MODEL, tier.getId()));
            if (model == null) return new ArrayList<>();

            BakedModel bakedModel = model.bake(variant);
            if (bakedModel == null) return new ArrayList<>();

            List<ModelQuadView> quads = bakedModel.getQuads(context);
            return quads != null ? quads : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
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

    private static JSONVariant createVariant(ForgeDirection facing) {
        int angleY = switch (facing) {
            case SOUTH -> 0;
            case EAST -> 270;
            case NORTH -> 180;
            case WEST -> 90;
            default -> 0;
        };

        return new JSONVariant(MODEL, 0, angleY, false);
    }

    @Override
    public ModelDisplay getDisplay(Position pos, BakedModelQuadContext context) {
        JSONModel model = ModelRegistry.getJSONModel(MODEL);

        if (model != null) {
            return model.bake(createVariant(ForgeDirection.SOUTH))
                .getDisplay(pos, context);
        }

        return ModelDisplay.DEFAULT;
    }

    @Override
    public IIcon getParticle(BakedModelQuadContext context) {
        JSONModel model = ModelRegistry.getJSONModel(MODEL);
        if (model != null) {
            return model.bake(createVariant(ForgeDirection.SOUTH))
                .getParticle(context);
        }

        return null;
    }
}

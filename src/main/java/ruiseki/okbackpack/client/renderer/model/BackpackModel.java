package ruiseki.okbackpack.client.renderer.model;

import static ruiseki.okbackpack.common.init.TierRegistries.LEATHER;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

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
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.BackpackPart;
import ruiseki.okbackpack.api.tier.BackpackTier;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okbackpack.api.wrapper.IModelUpgrade;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.common.block.TEBackpack;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;
import ruiseki.okbackpack.mixins.early.gtnhlib.JSONModelAccessor;
import ruiseki.okcore.datastructure.ThreadsafeCache;

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
        List<ModelQuadView> combinedQuads = new ArrayList<>();

        List<JSONVariant> variantsToRender = new ArrayList<>();

        ForgeDirection facing = ForgeDirection.SOUTH;
        BackpackTier tier = TierRegistry.getTier(LEATHER);
        if (context.getBlockState() != null) {
            facing = context.getBlockState()
                .getPropertyValue(BlockBackpack.DIRECTION_PROPERTY);
            tier = context.getBlockState()
                .getPropertyValue(BlockBackpack.TIER_PROPERTY);
        }

        Map<BackpackPart, List<ResourceLoc.ModelLoc>> upgradePartModels = new EnumMap<>(BackpackPart.class);

        BackpackWrapper wrapper = getWrapperFromContext(context);
        if (wrapper != null) {
            Map<Integer, IModelUpgrade> modelUpgrades = wrapper.gatherCapabilityUpgrades(IModelUpgrade.class);
            if (!modelUpgrades.isEmpty()) {
                for (IModelUpgrade modelWrapper : modelUpgrades.values()) {
                    Map<BackpackPart, List<ResourceLoc.ModelLoc>> activeModels = modelWrapper.geModels(context);
                    if (activeModels != null) {
                        for (var entry : activeModels.entrySet()) {
                            upgradePartModels.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                                .addAll(entry.getValue());
                        }
                    }
                }
            }
        }

        renderOrOverridePart(variantsToRender, BackpackPart.BASE, MODEL_BASE, facing, upgradePartModels);
        renderOrOverridePart(variantsToRender, BackpackPart.FRONT_POUCH, MODEL_FRONT, facing, upgradePartModels);
        renderOrOverridePart(variantsToRender, BackpackPart.LEFT_POUCH, MODEL_LEFT, facing, upgradePartModels);
        renderOrOverridePart(variantsToRender, BackpackPart.RIGHT_POUCH, MODEL_RIGHT, facing, upgradePartModels);

        for (JSONVariant variant : variantsToRender) {
            if (variant != null && variant.model() != null) {
                addPartQuads(combinedQuads, variant, context, tier);
            }
        }

        return combinedQuads;
    }

    private void renderOrOverridePart(List<JSONVariant> variantsToRender, BackpackPart part,
        ResourceLoc.ModelLoc defaultModel, ForgeDirection facing,
        Map<BackpackPart, List<ResourceLoc.ModelLoc>> upgradePartModels) {
        if (upgradePartModels.containsKey(part)) {
            List<ResourceLoc.ModelLoc> upgradeModels = upgradePartModels.get(part);
            for (ResourceLoc.ModelLoc modelLoc : upgradeModels) {
                variantsToRender.add(createVariantForPart(modelLoc, facing));
            }
        } else {
            variantsToRender.add(createVariantForPart(defaultModel, facing));
        }
    }

    private static JSONModel loadAndPrepareJSON(CacheKey key) {
        var baseJson = ModelRegistry.getJSONModel(key.modelLoc());
        if (baseJson == null) return null;

        JSONModelAccessor accessor = (JSONModelAccessor) baseJson;
        var modifiableTextures = new Object2ObjectOpenHashMap<>(baseJson.getTextures());
        modifiableTextures.put(
            "#clips",
            TierRegistry.getTier(key.tierId())
                .getClipTexturePath()
                .toString());

        return new JSONModel(
            accessor.getParentId(),
            accessor.isUseAO(),
            accessor.getDisplay(),
            modifiableTextures,
            accessor.getElements());
    }

    private void addPartQuads(List<ModelQuadView> targetList, JSONVariant variant, BakedModelQuadContext ctx,
        BackpackTier tier) {
        try {
            JSONModel modelToBake = JSON_CACHE.get(new CacheKey(variant.model(), tier.getId()));
            if (modelToBake == null) return;

            BakedModel bakedPart = modelToBake.bake(variant);
            if (bakedPart == null) return;

            List<ModelQuadView> partQuads = bakedPart.getQuads(ctx);
            if (partQuads != null) {
                targetList.addAll(partQuads);
            }
        } catch (Exception e) {
            OKBackpack.okLog(Level.ERROR, "Failed to render: " + variant.model(), e);
        }
    }

    private JSONVariant createVariantForPart(ResourceLoc.ModelLoc modelLoc, ForgeDirection facing) {
        int angleY = switch (facing) {
            case SOUTH -> 0;
            case EAST -> 270;
            case NORTH -> 180;
            case WEST -> 90;
            default -> 0;
        };
        return new JSONVariant(modelLoc, 0, angleY, false);
    }

    @Override
    public ModelDisplay getDisplay(Position pos, BakedModelQuadContext context) {
        JSONModel model = ModelRegistry.getJSONModel(MODEL_BASE);

        if (model != null) {
            return model.bake(createVariantForPart(MODEL_BASE, ForgeDirection.SOUTH))
                .getDisplay(pos, context);
        }
        return ModelDisplay.DEFAULT;
    }

    @Override
    public IIcon getParticle(BakedModelQuadContext context) {
        JSONModel model = ModelRegistry.getJSONModel(MODEL_BASE);

        if (model != null) {
            IIcon partical = model.bake(createVariantForPart(MODEL_BASE, ForgeDirection.SOUTH))
                .getParticle(context);
            return partical;
        }
        return null;
    }

    @Nullable
    public static BackpackWrapper getWrapperFromContext(BakedModelQuadContext context) {
        if (context instanceof BakedModelQuadContext.Item itemContext) {
            ItemStack stack = itemContext.getItemStack();
            return BackpackEntityHelpers.getWrapper(stack);
        } else if (context instanceof BakedModelQuadContext.World worldContext) {
            IBlockAccess world = worldContext.getWorld();
            if (world != null) {
                TileEntity te = world.getTileEntity(worldContext.getX(), worldContext.getY(), worldContext.getZ());
                if (te instanceof TEBackpack teBackpack) {
                    return teBackpack.getWrapper();
                }
            }
        }

        return null;
    }
}

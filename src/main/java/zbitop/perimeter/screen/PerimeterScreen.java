package zbitop.perimeter.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import zbitop.perimeter.network.PerimeterSizePayload;

public class PerimeterScreen extends AbstractContainerScreen<PerimeterMenu> {

    private EditBox sizeField;
    private String lastValidValue;

    public PerimeterScreen(PerimeterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        int fieldX = leftPos + 40;
        int fieldY = topPos + 30;

        this.lastValidValue = String.valueOf(menu.getBlockEntityRef().getSize());

        this.sizeField = new EditBox(this.font, fieldX, fieldY, 80, 20, Component.literal("Tamaño"));
        this.sizeField.setValue(this.lastValidValue);

        this.sizeField.setResponder(text -> {
            if (text.matches("\\d*")) {
                this.lastValidValue = text; // texto válido (solo dígitos, o vacío), lo aceptamos
            } else {
                this.sizeField.setValue(this.lastValidValue); // texto inválido, lo revertimos
            }
        });

        this.addRenderableWidget(this.sizeField);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xC0101010);
        graphics.text(this.font, "Tamaño del perímetro:", leftPos + 10, topPos + 15, 0xFFFFFFFF, false);
    }

    @Override
    public void onClose() {
        try {
            int newSize = Integer.parseInt(this.lastValidValue);
            ClientPlayNetworking.send(new PerimeterSizePayload(menu.getBlockEntityRef().getBlockPos(), newSize));
        } catch (NumberFormatException e) {
            // campo vacío, no mandamos nada
        }
        super.onClose();
    }
}
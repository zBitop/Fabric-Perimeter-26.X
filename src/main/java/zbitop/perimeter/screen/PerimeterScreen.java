package zbitop.perimeter.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import zbitop.perimeter.network.PerimeterSizePayload;
import zbitop.perimeter.network.PerimeterWithdrawPayload;

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

        this.addRenderableWidget(
                Button.builder(Component.literal("Confirmar"), button -> this.onConfirm())
                        .bounds(fieldX, fieldY + 30, 80, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Retirar objetos"), button -> this.onWithdraw())
                        .bounds(fieldX, fieldY + 55, 100, 20)
                        .build()
        );
    }

    private void onWithdraw() {
        ClientPlayNetworking.send(new PerimeterWithdrawPayload(menu.getBlockEntityRef().getBlockPos()));

        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.closeContainer();
        }
    }

    private void onConfirm() {
        try {
            int newSize = Integer.parseInt(this.lastValidValue);
            ClientPlayNetworking.send(new PerimeterSizePayload(menu.getBlockEntityRef().getBlockPos(), newSize));
        } catch (NumberFormatException e) {
            // campo vacío o inválido, no mandamos nada
            return;
        }

        // cierra la GUI (server + cliente) sin volver a pasar por onClose de forma rara
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.closeContainer();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xC0101010);
        graphics.text(this.font, "Tamaño del perímetro:", leftPos + 10, topPos + 15, 0xFFFFFFFF, false);

        int barX = leftPos + 40;
        int barY = topPos + 90;
        int barWidth = 100;
        int barHeight = 12;

        int percent = menu.getProgressPercent();
        boolean mining = menu.getBlockEntityRef().isMining();

        // fondo de la barra
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF303030);

        // relleno según el progreso
        int filledWidth = barWidth * Math.max(0, Math.min(100, percent)) / 100;
        if (filledWidth > 0) {
            graphics.fill(barX, barY, barX + filledWidth, barY + barHeight, 0xFF3AA13A);
        }

        // borde
        graphics.fill(barX, barY, barX + barWidth, barY + 1, 0xFFFFFFFF);
        graphics.fill(barX, barY + barHeight - 1, barX + barWidth, barY + barHeight, 0xFFFFFFFF);
        graphics.fill(barX, barY, barX + 1, barY + barHeight, 0xFFFFFFFF);
        graphics.fill(barX + barWidth - 1, barY, barX + barWidth, barY + barHeight, 0xFFFFFFFF);

        String label = mining ? (percent + "%") : "Sin minado activo";
        int labelWidth = this.font.width(label);
        graphics.text(this.font, label, barX + (barWidth - labelWidth) / 2, barY + 2, 0xFFFFFFFF, false);
    }

    @Override
    public void onClose() {
        // Ya no mandamos el paquete acá: eso ahora lo hace el botón "Confirmar" (onConfirm()).
        // Así, salir con ESC / E / al alejarse del bloque NO dispara el minado.
        super.onClose();
    }
}
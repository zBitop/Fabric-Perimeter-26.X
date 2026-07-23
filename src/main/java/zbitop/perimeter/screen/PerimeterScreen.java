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
        // Pasamos Component.empty() en vez del 'title' recibido (que sería el nombre
        // traducido del bloque, ej. "block.perimeter.perimeter") para que no se dibuje
        // ningún título por defecto encima de nuestro panel.
        super(menu, playerInventory, Component.empty());
        // Este campo sí es mutable: así tampoco se dibuja el "Inventario" por defecto.
        this.inventoryLabelX = Integer.MIN_VALUE / 2;
    }

    @Override
    protected void init() {
        super.init();

        int contentX = leftPos + 18;
        int fieldY = topPos + 34;

        this.lastValidValue = String.valueOf(menu.getBlockEntityRef().getSize());

        this.sizeField = new EditBox(this.font, contentX, fieldY, imageWidth - 36, 20, Component.literal("Tamaño"));
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
                        .bounds(contentX, fieldY + 26, imageWidth - 36, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Retirar objetos"), button -> this.onWithdraw())
                        .bounds(contentX, fieldY + 50, imageWidth - 36, 20)
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

        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xE0101010);
        // borde del panel
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, 0xFF565656);
        graphics.fill(leftPos, topPos + imageHeight - 1, leftPos + imageWidth, topPos + imageHeight, 0xFF565656);
        graphics.fill(leftPos, topPos, leftPos + 1, topPos + imageHeight, 0xFF565656);
        graphics.fill(leftPos + imageWidth - 1, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF565656);

        // título propio, centrado (reemplaza al título default del bloque)
        String header = "Perímetro";
        int headerWidth = this.font.width(header);
        graphics.text(this.font, header, leftPos + (imageWidth - headerWidth) / 2, topPos + 8, 0xFFFFFFFF, false);

        graphics.text(this.font, "Tamaño:", leftPos + 18, topPos + 24, 0xFFAAAAAA, false);

        int barX = leftPos + 18;
        int barY = topPos + 110;
        int barWidth = imageWidth - 36;
        int barHeight = 12;

        int percent = menu.getProgressPercent();
        boolean mining = menu.isMining();

        // fondo de la barra
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF303030);

        // relleno según el progreso
        int filledWidth = barWidth * Math.max(0, Math.min(100, percent)) / 100;
        if (filledWidth > 0) {
            graphics.fill(barX, barY, barX + filledWidth, barY + barHeight, 0xFF3AA13A);
        }

        // borde de la barra
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
package fi.dy.masa.litematica.gui;

import java.io.File;
import javax.annotation.Nullable;
import org.lwjgl.input.Keyboard;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.ISchematic;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.util.GuiUtils;
import fi.dy.masa.malilib.gui.util.Message.MessageType;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntryType;
import fi.dy.masa.malilib.gui.widgets.WidgetTextFieldBase;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.StringUtils;

public abstract class GuiSchematicSaveBase extends GuiSchematicBrowserBase implements ISelectionListener<DirectoryEntry>
{
    @Nullable protected final ISchematic schematic;
    protected WidgetTextFieldBase textField;
    protected String lastText = "";
    protected String defaultText = "";
    protected boolean updatePlacementsOption;

    public GuiSchematicSaveBase(@Nullable ISchematic schematic)
    {
        this(schematic, 10, 70);
    }

    public GuiSchematicSaveBase(@Nullable ISchematic schematic, int listX, int listY)
    {
        super(listX, listY);

        this.schematic = schematic;

        this.textField = new WidgetTextFieldBase(10, 32, 160, 20);
        this.textField.setFocused(true);
    }

    public void setUpdatePlacementsOption(boolean updatePlacements)
    {
        this.updatePlacementsOption = updatePlacements;
    }

    @Override
    public int getBrowserHeight()
    {
        return this.height - 80;
    }

    @Override
    public int getMaxInfoHeight()
    {
        return super.getMaxInfoHeight();
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.textField.setWidth(this.width - 196);
        this.addWidget(this.textField);

        DirectoryEntry entry = this.getListWidget().getLastSelectedEntry();

        // Only set the text field contents if it hasn't been set already.
        // This prevents overwriting any user input text when switching to a newly created directory.
        if (this.lastText.isEmpty())
        {
            if (entry != null && entry.getType() != DirectoryEntryType.DIRECTORY && entry.getType() != DirectoryEntryType.INVALID)
            {
                this.setTextFieldText(FileUtils.getNameWithoutExtension(entry.getName()));
            }
            else if (this.schematic != null && this.schematic.getFile() != null)
            {
                this.setTextFieldText(FileUtils.getNameWithoutExtension(this.schematic.getFile().getName()));
            }
            else
            {
                this.setTextFieldText(this.defaultText);
            }
        }

        this.createCustomElements();
    }

    protected void refreshList()
    {
        if (GuiUtils.getCurrentScreen() == this)
        {
            this.getListWidget().refreshEntries();
            this.getListWidget().clearSchematicMetadataCache();
        }
    }

    protected void createCustomElements()
    {
        int x = this.textField.getX() + this.textField.getWidth() + 12;
        int y = 32;

        this.createButton(x, y, ButtonType.SAVE);
    }

    protected void setTextFieldText(String text)
    {
        this.lastText = text;
        this.textField.setText(text);
    }

    protected String getTextFieldText()
    {
        return this.textField.getText();
    }

    protected void updateDependentPlacements(File newSchematicFile, boolean selectedOnly)
    {
        if (this.schematic != null)
        {
            SchematicPlacementManager manager = DataManager.getSchematicPlacementManager();

            if (selectedOnly)
            {
                SchematicPlacement placement = manager.getSelectedSchematicPlacement();

                if (placement != null && placement.getSchematic() == this.schematic)
                {
                    placement.setSchematicFile(newSchematicFile);
                }
            }
            else
            {
                for (SchematicPlacement placement : manager.getAllPlacementsOfSchematic(this.schematic))
                {
                    placement.setSchematicFile(newSchematicFile);
                }
            }
        }
    }

    protected abstract void saveSchematic();

    protected int createButton(int x, int y, ButtonType type)
    {
        String label = StringUtils.translate(type.getLabelKey());
        int width = this.getStringWidth(label) + 10;

        ButtonGeneric button;

        if (type == ButtonType.SAVE)
        {
            button = new ButtonGeneric(x, y, width, 20, label, "litematica.gui.label.schematic_save.hoverinfo.hold_shift_to_overwrite");
        }
        else
        {
            button = new ButtonGeneric(x, y, width, 20, label);
        }

        this.addButton(button, (btn, mbtn) -> this.saveSchematic());

        return x + width + 4;
    }

    @Override
    public void setString(String string)
    {
        this.setNextMessageType(MessageType.ERROR);
        super.setString(string);
    }

    @Override
    public void onSelectionChange(@Nullable DirectoryEntry entry)
    {
        if (entry != null && entry.getType() != DirectoryEntryType.DIRECTORY && entry.getType() != DirectoryEntryType.INVALID)
        {
            this.setTextFieldText(FileUtils.getNameWithoutExtension(entry.getName()));
        }
    }

    @Override
    protected ISelectionListener<DirectoryEntry> getSelectionListener()
    {
        return this;
    }

    @Override
    public boolean onKeyTyped(char typedChar, int keyCode)
    {
        if (this.textField.isFocused() && keyCode == Keyboard.KEY_RETURN)
        {
            this.saveSchematic();
            return true;
        }
        else if (this.textField.onKeyTyped(typedChar, keyCode))
        {
            this.getListWidget().clearSelection();
            return true;
        }

        return super.onKeyTyped(typedChar, keyCode);
    }

    public enum ButtonType
    {
        SAVE ("litematica.gui.button.save_schematic");

        private final String labelKey;

        private ButtonType(String labelKey)
        {
            this.labelKey = labelKey;
        }

        public String getLabelKey()
        {
            return this.labelKey;
        }
    }
}

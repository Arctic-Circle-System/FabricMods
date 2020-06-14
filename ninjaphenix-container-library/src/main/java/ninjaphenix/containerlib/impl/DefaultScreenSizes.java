package ninjaphenix.containerlib.impl;

import net.minecraft.util.Identifier;
import ninjaphenix.containerlib.api.*;
import ninjaphenix.containerlib.api.screen.PagedScreenMeta;
import ninjaphenix.containerlib.api.screen.SingleScreenMeta;
import ninjaphenix.containerlib.impl.inventory.PagedContainer;
import ninjaphenix.containerlib.impl.inventory.ScrollableContainer;
import ninjaphenix.containerlib.impl.inventory.SingleContainer;

import static ninjaphenix.containerlib.api.Constants.*;

public final class DefaultScreenSizes implements ContainerLibraryExtension
{
    private final ContainerLibraryAPI API = ContainerLibraryAPI.INSTANCE;

    @Override
    public void declareScreenSizeCallbacks()
    {
        API.declareScreenSizeRegisterCallback(SINGLE_CONTAINER, SingleContainer::onScreenSizeRegistered);
        API.declareScreenSizeRegisterCallback(PAGED_CONTAINER, PagedContainer::onScreenSizeRegistered);
        API.declareScreenSizeRegisterCallback(SCROLLABLE_CONTAINER, ScrollableContainer::onScreenSizeRegistered);
    }

    @Override
    public void declareScreenSizes()
    {
        API.declareScreenSize(SINGLE_CONTAINER, new SingleScreenMeta(9, 3, 27, getTexture("shared", 9, 3), 208, 192)); // Wood
        API.declareScreenSize(SINGLE_CONTAINER, new SingleScreenMeta(9, 6, 54, getTexture("shared", 9, 6), 208, 240)); // Iron / Large Wood
        API.declareScreenSize(SINGLE_CONTAINER, new SingleScreenMeta(9, 9, 81, getTexture("shared", 9, 9), 208, 304)); // Gold
        API.declareScreenSize(SINGLE_CONTAINER, new SingleScreenMeta(12, 9, 108, getTexture("shared", 12, 9), 256, 304)); // Diamond / Large Iron
        API.declareScreenSize(SINGLE_CONTAINER, new SingleScreenMeta(18, 9, 162, getTexture("shared", 18, 9), 368, 304)); // Large Gold
        API.declareScreenSize(SINGLE_CONTAINER, new SingleScreenMeta(18, 12, 216, getTexture("shared", 18, 12), 368, 352)); // Large Diamond

        API.declareScreenSize(PAGED_CONTAINER, new PagedScreenMeta(9, 8, 3, 216, getTexture("shared", 9, 8), 0, 0)); // Large Diamond
    }

    private Identifier getTexture(String type, int width, int height)
    {
        return new Identifier("ninjaphenix-container-lib", String.format("textures/gui/container/%s_%d_%d.png", type, width, height));
    }
}
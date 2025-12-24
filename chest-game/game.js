// Game State
const gameState = {
    gold: 100,
    chestsOpened: 0,
    totalValue: 0,
    inventory: [],
    achievements: [],
    collection: {
        weapons: new Set(),
        armor: new Set(),
        potions: new Set(),
        special: new Set()
    }
};

// Chest Types
const chestTypes = [
    {
        name: 'Wooden Chest',
        rarity: 'common',
        cost: 10,
        icon: '📦',
        itemCount: { min: 1, max: 2 },
        rarityWeights: { common: 70, uncommon: 25, rare: 4, epic: 1, legendary: 0, mythic: 0 }
    },
    {
        name: 'Bronze Chest',
        rarity: 'uncommon',
        cost: 50,
        icon: '🎁',
        itemCount: { min: 2, max: 3 },
        rarityWeights: { common: 40, uncommon: 40, rare: 15, epic: 4, legendary: 1, mythic: 0 }
    },
    {
        name: 'Silver Chest',
        rarity: 'rare',
        cost: 200,
        icon: '💎',
        itemCount: { min: 3, max: 4 },
        rarityWeights: { common: 20, uncommon: 35, rare: 30, epic: 12, legendary: 3, mythic: 0 }
    },
    {
        name: 'Golden Chest',
        rarity: 'epic',
        cost: 1000,
        icon: '👑',
        itemCount: { min: 4, max: 5 },
        rarityWeights: { common: 5, uncommon: 20, rare: 35, epic: 30, legendary: 9, mythic: 1 }
    },
    {
        name: 'Diamond Chest',
        rarity: 'legendary',
        cost: 5000,
        icon: '✨',
        itemCount: { min: 5, max: 7 },
        rarityWeights: { common: 0, uncommon: 10, rare: 25, epic: 35, legendary: 25, mythic: 5 }
    }
];

// Item Templates
const itemTemplates = {
    weapons: {
        icon: '⚔️',
        names: ['Sword', 'Bow', 'Staff', 'Dagger', 'Axe', 'Hammer', 'Spear', 'Crossbow', 'Wand', 'Katana'],
        prefixes: ['Rusty', 'Sharp', 'Blessed', 'Cursed', 'Ancient', 'Mystic', 'Dragon', 'Phoenix', 'Shadow', 'Light'],
        baseValue: 20
    },
    armor: {
        icon: '🛡️',
        names: ['Helmet', 'Chestplate', 'Boots', 'Gauntlets', 'Leggings', 'Shield', 'Pauldrons', 'Bracers'],
        prefixes: ['Leather', 'Iron', 'Steel', 'Mithril', 'Dragon', 'Divine', 'Demon', 'Crystal', 'Obsidian'],
        baseValue: 25
    },
    potions: {
        icon: '🧪',
        names: ['Health Potion', 'Mana Potion', 'Strength Potion', 'Speed Potion', 'Defense Potion', 'Luck Potion'],
        prefixes: ['Minor', 'Lesser', 'Greater', 'Superior', 'Supreme', 'Ultimate'],
        baseValue: 15
    },
    special: {
        icon: '🌟',
        names: ['Pet', 'Mount', 'Artifact', 'Relic', 'Gem', 'Crown', 'Ring', 'Amulet', 'Orb', 'Scroll'],
        prefixes: ['Rare', 'Exotic', 'Legendary', 'Mythical', 'Celestial', 'Infernal', 'Ethereal', 'Prismatic'],
        baseValue: 50
    }
};

// Rarity Multipliers
const rarityMultipliers = {
    common: { multiplier: 1, color: '#9d9d9d', name: 'Common' },
    uncommon: { multiplier: 2, color: '#1eff00', name: 'Uncommon' },
    rare: { multiplier: 5, color: '#0070dd', name: 'Rare' },
    epic: { multiplier: 10, color: '#a335ee', name: 'Epic' },
    legendary: { multiplier: 25, color: '#ff8000', name: 'Legendary' },
    mythic: { multiplier: 100, color: '#e6cc80', name: 'Mythic' }
};

// Achievements
const achievementsList = [
    { id: 'first_chest', name: 'First Steps', desc: 'Open your first chest', icon: '🎉', check: (state) => state.chestsOpened >= 1 },
    { id: 'chest_10', name: 'Enthusiast', desc: 'Open 10 chests', icon: '📦', check: (state) => state.chestsOpened >= 10 },
    { id: 'chest_50', name: 'Collector', desc: 'Open 50 chests', icon: '🎁', check: (state) => state.chestsOpened >= 50 },
    { id: 'chest_100', name: 'Master Hunter', desc: 'Open 100 chests', icon: '👑', check: (state) => state.chestsOpened >= 100 },
    { id: 'rich', name: 'Wealthy', desc: 'Accumulate 10,000 gold', icon: '💰', check: (state) => state.gold >= 10000 },
    { id: 'legendary_find', name: 'Legendary Find', desc: 'Find a Legendary item', icon: '⭐', check: (state) => state.inventory.some(item => item.rarity === 'legendary') },
    { id: 'mythic_find', name: 'Mythic Discovery', desc: 'Find a Mythic item', icon: '✨', check: (state) => state.inventory.some(item => item.rarity === 'mythic') },
    { id: 'full_weapon', name: 'Arsenal', desc: 'Collect 10 different weapons', icon: '⚔️', check: (state) => state.collection.weapons.size >= 10 },
    { id: 'value_100k', name: 'Fortune', desc: 'Earn 100,000 total value', icon: '💎', check: (state) => state.totalValue >= 100000 }
];

// Utility Functions
function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function weightedRandom(weights) {
    const total = Object.values(weights).reduce((sum, weight) => sum + weight, 0);
    let random = Math.random() * total;

    for (const [key, weight] of Object.entries(weights)) {
        random -= weight;
        if (random <= 0) return key;
    }
    return Object.keys(weights)[0];
}

function generateItem(chestRarity) {
    const typeKeys = Object.keys(itemTemplates);
    const type = typeKeys[randomInt(0, typeKeys.length - 1)];
    const template = itemTemplates[type];

    const rarityWeights = chestTypes.find(c => c.rarity === chestRarity).rarityWeights;
    const itemRarity = weightedRandom(rarityWeights);

    const prefix = template.prefixes[randomInt(0, template.prefixes.length - 1)];
    const name = template.names[randomInt(0, template.names.length - 1)];

    const rarityInfo = rarityMultipliers[itemRarity];
    const value = Math.floor(template.baseValue * rarityInfo.multiplier * (0.8 + Math.random() * 0.4));

    return {
        id: Date.now() + Math.random(),
        type: type,
        name: `${prefix} ${name}`,
        rarity: itemRarity,
        rarityName: rarityInfo.name,
        icon: template.icon,
        value: value,
        color: rarityInfo.color
    };
}

function openChest(chest) {
    if (gameState.gold < chest.cost) {
        alert('Not enough gold!');
        return;
    }

    gameState.gold -= chest.cost;
    gameState.chestsOpened++;

    const itemCount = randomInt(chest.itemCount.min, chest.itemCount.max);
    const items = [];

    for (let i = 0; i < itemCount; i++) {
        items.push(generateItem(chest.rarity));
    }

    items.forEach(item => {
        gameState.inventory.push(item);
        gameState.totalValue += item.value;
        gameState.collection[item.type].add(item.name);
    });

    showChestOpening(chest, items);
    updateUI();
    checkAchievements();
    saveGame();
}

function showChestOpening(chest, items) {
    const openingArea = document.getElementById('openingArea');
    const chestAnimation = document.getElementById('chestAnimation');
    const rewardDisplay = document.getElementById('rewardDisplay');

    openingArea.classList.remove('hidden');
    chestAnimation.innerHTML = chest.icon;
    chestAnimation.classList.add('opening');
    rewardDisplay.innerHTML = '';

    setTimeout(() => {
        chestAnimation.classList.remove('opening');
        items.forEach((item, index) => {
            setTimeout(() => {
                const itemDiv = document.createElement('div');
                itemDiv.className = `reward-item rarity-${item.rarity}`;
                itemDiv.innerHTML = `
                    <div class="item-icon">${item.icon}</div>
                    <div class="item-name">${item.name}</div>
                    <div class="item-rarity">${item.rarityName}</div>
                    <div class="item-value">${item.value} gold</div>
                `;
                rewardDisplay.appendChild(itemDiv);
            }, index * 200);
        });
    }, 1000);
}

function sellItem(itemId) {
    const itemIndex = gameState.inventory.findIndex(item => item.id === itemId);
    if (itemIndex !== -1) {
        const item = gameState.inventory[itemIndex];
        gameState.gold += item.value;
        gameState.inventory.splice(itemIndex, 1);
        updateUI();
        saveGame();
    }
}

function sellAllItems() {
    if (gameState.inventory.length === 0) {
        alert('No items to sell!');
        return;
    }

    const totalValue = gameState.inventory.reduce((sum, item) => sum + item.value, 0);
    if (confirm(`Sell all ${gameState.inventory.length} items for ${totalValue} gold?`)) {
        gameState.gold += totalValue;
        gameState.inventory = [];
        updateUI();
        saveGame();
    }
}

function sortInventory() {
    gameState.inventory.sort((a, b) => b.value - a.value);
    updateUI();
}

function checkAchievements() {
    achievementsList.forEach(achievement => {
        if (!gameState.achievements.includes(achievement.id) && achievement.check(gameState)) {
            gameState.achievements.push(achievement.id);
            showAchievementUnlock(achievement);
        }
    });
    updateAchievements();
}

function showAchievementUnlock(achievement) {
    // Could add a notification popup here
    console.log(`Achievement unlocked: ${achievement.name}`);
}

// UI Update Functions
function updateUI() {
    document.getElementById('gold').textContent = gameState.gold.toLocaleString();
    document.getElementById('chestsOpened').textContent = gameState.chestsOpened.toLocaleString();
    document.getElementById('totalValue').textContent = gameState.totalValue.toLocaleString();

    updateChestShop();
    updateInventory();
    updateCollection();
}

function updateChestShop() {
    const chestGrid = document.getElementById('chestGrid');
    chestGrid.innerHTML = '';

    chestTypes.forEach(chest => {
        const canAfford = gameState.gold >= chest.cost;
        const chestDiv = document.createElement('div');
        chestDiv.className = `chest-card ${canAfford ? '' : 'disabled'}`;
        chestDiv.setAttribute('data-rarity', chest.rarity);
        chestDiv.innerHTML = `
            <div class="chest-info">
                <div class="chest-name">${chest.name}</div>
                <div class="chest-price">💰 ${chest.cost} gold</div>
                <div class="chest-chance">${chest.itemCount.min}-${chest.itemCount.max} items</div>
            </div>
            <div class="chest-icon">${chest.icon}</div>
        `;

        if (canAfford) {
            chestDiv.addEventListener('click', () => openChest(chest));
        }

        chestGrid.appendChild(chestDiv);
    });
}

function updateInventory() {
    const inventoryGrid = document.getElementById('inventoryGrid');
    inventoryGrid.innerHTML = '';

    if (gameState.inventory.length === 0) {
        inventoryGrid.innerHTML = '<p class="empty-message">Your inventory is empty. Open some chests!</p>';
        return;
    }

    gameState.inventory.forEach(item => {
        const itemDiv = document.createElement('div');
        itemDiv.className = `inventory-item rarity-${item.rarity}`;
        itemDiv.innerHTML = `
            <button class="sell-btn" onclick="sellItem(${item.id})">Sell</button>
            <div class="item-icon">${item.icon}</div>
            <div class="item-name">${item.name}</div>
            <div class="item-type">${item.type}</div>
            <div class="item-rarity">${item.rarityName}</div>
            <div class="item-value">${item.value} 💰</div>
        `;
        inventoryGrid.appendChild(itemDiv);
    });
}

function updateAchievements() {
    const achievementGrid = document.getElementById('achievementGrid');
    achievementGrid.innerHTML = '';

    achievementsList.forEach(achievement => {
        const unlocked = gameState.achievements.includes(achievement.id);
        const achDiv = document.createElement('div');
        achDiv.className = `achievement-card ${unlocked ? 'unlocked' : ''}`;
        achDiv.innerHTML = `
            <div class="achievement-icon">${achievement.icon}</div>
            <div class="achievement-name">${achievement.name}</div>
            <div class="achievement-desc">${achievement.desc}</div>
        `;
        achievementGrid.appendChild(achDiv);
    });
}

function updateCollection() {
    const collectionStats = document.getElementById('collectionStats');
    collectionStats.innerHTML = '';

    const categories = [
        { key: 'weapons', name: 'Weapons', icon: '⚔️', total: itemTemplates.weapons.names.length * itemTemplates.weapons.prefixes.length },
        { key: 'armor', name: 'Armor', icon: '🛡️', total: itemTemplates.armor.names.length * itemTemplates.armor.prefixes.length },
        { key: 'potions', name: 'Potions', icon: '🧪', total: itemTemplates.potions.names.length * itemTemplates.potions.prefixes.length },
        { key: 'special', name: 'Special', icon: '🌟', total: itemTemplates.special.names.length * itemTemplates.special.prefixes.length }
    ];

    categories.forEach(category => {
        const collected = gameState.collection[category.key].size;
        const percentage = Math.floor((collected / category.total) * 100);

        const catDiv = document.createElement('div');
        catDiv.className = 'collection-category';
        catDiv.innerHTML = `
            <div class="category-name">${category.icon} ${category.name}</div>
            <div class="progress-bar">
                <div class="progress-fill" style="width: ${percentage}%">
                    ${collected}/${category.total}
                </div>
            </div>
        `;
        collectionStats.appendChild(catDiv);
    });
}

// Save/Load Functions
function saveGame() {
    const saveData = {
        ...gameState,
        collection: {
            weapons: Array.from(gameState.collection.weapons),
            armor: Array.from(gameState.collection.armor),
            potions: Array.from(gameState.collection.potions),
            special: Array.from(gameState.collection.special)
        }
    };
    localStorage.setItem('mysticChestHunter', JSON.stringify(saveData));
}

function loadGame() {
    const saveData = localStorage.getItem('mysticChestHunter');
    if (saveData) {
        const data = JSON.parse(saveData);
        gameState.gold = data.gold;
        gameState.chestsOpened = data.chestsOpened;
        gameState.totalValue = data.totalValue;
        gameState.inventory = data.inventory;
        gameState.achievements = data.achievements;
        gameState.collection = {
            weapons: new Set(data.collection.weapons),
            armor: new Set(data.collection.armor),
            potions: new Set(data.collection.potions),
            special: new Set(data.collection.special)
        };
    }
}

// Event Listeners
document.getElementById('sortBtn').addEventListener('click', sortInventory);
document.getElementById('sellAllBtn').addEventListener('click', sellAllItems);

// Make sellItem available globally
window.sellItem = sellItem;

// Initialize Game
loadGame();
updateUI();
updateAchievements();

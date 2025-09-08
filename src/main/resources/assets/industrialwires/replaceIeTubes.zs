//add component to wb
mods.immersiveengineering.Blueprint.addRecipe(
	"components",
	<industrialwires:stuff_crafting:3>,
	[<ore:plateNickel>, <ore:wireCopper>, <ore:dustRedstone>]
);
//remove vaccum from wb
mods.immersiveengineering.Blueprint.removeRecipe(<immersiveengineering:material:26>);
//remove vaccum from pa
mods.immersiveintelligence.PrecisionAssembler.removeRecipe(<immersiveengineering:material:26>);
//add component to pa
mods.immersiveintelligence.PrecisionAssembler.addRecipe(
	<industrialwires:stuff_crafting:3>*4,
	<immersiveengineering:metal:20>*3,
	[<ore:plateIron>*3, <ore:wireCopper>*6, <ore:dustRedstone>*3],
	["inserter", "solderer", "drill"],
	["drill work main", "solderer work first", "inserter pick first", "inserter drop main", "solderer work main", "drill work second", "inserter pick second", "inserter drop main"], 
	36000, 3
);
//add adv component to pa
mods.immersiveintelligence.PrecisionAssembler.addRecipe(
	<industrialwires:stuff_crafting:5>,
	null,
	[<ore:plateSteel>*3, <ore:wireTungsten>*3, <industrialwires:stuff_crafting:3>*2],
	["inserter", "solderer", "drill"],
	["drill work main", "inserter pick second", "inserter drop main", "inserter pick first", "inserter drop main", "solderer work main"],
	72000, 3.75
);
//remove advanced from pa
mods.immersiveintelligence.PrecisionAssembler.removeRecipe(<immersiveintelligence:material>);
//remove fluorescent
recipes.removeByRecipeName("immersiveengineering:tool/fluorescent_tube");
//add neon component
recipes.addShaped(
	"ct_neonComponent",
	<industrialwires:stuff_crafting:4>,
	[
		[null, <ore:nuggetElectrum>, null],
		[null, <ore:dustFluorite>, null],
		[null, <ore:nuggetElectrum>, null]
	]
);
//remove scheme for vaccum tube
recipes.removeByRecipeName("immersiveintelligence:blueprints/schemes/vacuum_tube");
//remove scheme for adv tube
recipes.removeByRecipeName("immersiveintelligence:blueprints/schemes/advanced_vacuum_tube");
//add recipe for vaccum's scheme
recipes.addShaped(
	"iw_vaccumScheme",
	<immersiveintelligence:assembly_scheme>.withTag({recipeItem: {id: "industrialwires:stuff_crafting", Count: 3 as byte, Damage: 3 as short}}),
	[
		[<ore:wireCopper>, <ore:plateIron>, <ore:wireCopper>],
		[<ore:dyeBlue>, <ore:dyeBlue>, <ore:dyeBlue>],
		[<ore:paper>, <ore:paper>, <ore:paper>]
	]
);
//add recipe for adv's scheme
recipes.addShaped(
	"iw_advScheme",
	<immersiveintelligence:assembly_scheme>.withTag({recipeItem: {id: "industrialwires:stuff_crafting", Count: 1 as byte, Damage: 5 as short}}),
	[
		[<immersiveengineering:material:26>, <ore:plateAdvancedEletronicAlloy>, <immersiveengineering:material:26>],
		[<ore:dyeBlue>, <ore:dyeBlue>, <ore:dyeBlue>],
		[<ore:paper>, <ore:paper>, <ore:paper>]
	]
);
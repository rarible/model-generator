type LazyErc721 = {
	type: "ERC721"
	contract: Address
	tokenId: string
}

type LazyERC1155 = {
	type: "ERC1155"
	contract: Address
	tokenId: string
}

type LazyNFT = LazyErc721 | LazyERC1155
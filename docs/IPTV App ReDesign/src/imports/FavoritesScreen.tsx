import svgPaths from "./svg-ctt49saaa0";
import imgImage from "figma:asset/c7dfd8af197f728a7906263625ce373b649ad0f2.png";
import imgImage1 from "figma:asset/ffe1f90cd775f69f5226850f42a3ba6ec78d3e46.png";
import imgImage2 from "figma:asset/edfca41ede6681f2c8bc5cd024bfc06fdd105cb1.png";
import imgImage3 from "figma:asset/c29ddba59e76568743b8fa2b81a1b11951c86db7.png";
import imgImage4 from "figma:asset/26362cec23c41366e8626c8fe093da85bbeb45b6.png";
import imgImage5 from "figma:asset/2c79ca1bbbd52c630b8a27759f9dc216e49acd9c.png";

function Image({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="aspect-square relative rounded-[8px] shrink-0 w-full">
      <div className="absolute inset-0 overflow-hidden pointer-events-none rounded-[8px]">{children}</div>
    </div>
  );
}

function Container1({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative shrink-0 size-[18px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 18 18">
        <g id="Container">{children}</g>
      </svg>
    </div>
  );
}
type ContainerText1Props = {
  text: string;
};

function ContainerText1({ text }: ContainerText1Props) {
  return (
    <div className="content-stretch flex flex-col items-start relative shrink-0 w-full">
      <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal justify-center leading-[0] not-italic relative shrink-0 text-[#94a3b8] text-[14px] w-full">
        <p className="leading-[21px]">{text}</p>
      </div>
    </div>
  );
}
type ContainerTextProps = {
  text: string;
};

function ContainerText({ text }: ContainerTextProps) {
  return (
    <div className="content-stretch flex flex-col items-start relative shrink-0 w-full">
      <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium justify-center leading-[0] not-italic relative shrink-0 text-[16px] text-white w-full">
        <p className="leading-[24px]">{text}</p>
      </div>
    </div>
  );
}

function Container() {
  return (
    <div className="h-[15.833px] relative shrink-0 w-[16.667px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 16.6667 15.8333">
        <g id="Container">
          <path d={svgPaths.p507c800} fill="var(--fill-0, #FACC15)" id="Icon" />
        </g>
      </svg>
    </div>
  );
}

export default function FavoritesScreen() {
  return (
    <div className="bg-[#101c22] content-stretch flex flex-col items-start relative size-full" data-name="Favorites Screen">
      <div className="h-[884px] min-h-[884px] overflow-clip relative shrink-0 w-full" data-name="Container">
        <div className="absolute content-stretch flex flex-col inset-[120px_0_-30.5px_0] items-start" data-name="Main Content">
          <div className="relative shrink-0 w-full" data-name="Heading 3 - Section Header">
            <div className="content-stretch flex flex-col items-start pb-[8px] pt-[16px] px-[16px] relative w-full">
              <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold justify-center leading-[0] not-italic relative shrink-0 text-[18px] text-white tracking-[-0.27px] w-full">
                <p className="leading-[22.5px]">All Channels</p>
              </div>
            </div>
          </div>
          <div className="h-[748px] relative shrink-0 w-full" data-name="Image Grid">
            <div className="absolute content-stretch flex flex-col gap-[12px] inset-[16px_203px_504px_16px] items-start" data-name="Container">
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <Image>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgImage} />
                </Image>
                <div className="absolute bg-[rgba(0,0,0,0.5)] content-stretch flex items-center justify-center right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
                  <Container />
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="Channel One" />
                <ContainerText1 text="Main" />
              </div>
            </div>
            <div className="absolute content-stretch flex flex-col gap-[12px] inset-[16px_16px_504px_203px] items-start" data-name="Container">
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <Image>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgImage1} />
                </Image>
                <div className="absolute bg-[rgba(0,0,0,0.5)] content-stretch flex items-center justify-center right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
                  <Container />
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="Channel Two" />
                <ContainerText1 text="Sports" />
              </div>
            </div>
            <div className="absolute content-stretch flex flex-col gap-[12px] inset-[260px_203px_260px_16px] items-start" data-name="Container">
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <Image>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgImage2} />
                </Image>
                <div className="absolute bg-[rgba(0,0,0,0.5)] content-stretch flex items-center justify-center right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
                  <Container />
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="Channel Three" />
                <ContainerText1 text="Kids" />
              </div>
            </div>
            <div className="absolute content-stretch flex flex-col gap-[12px] inset-[260px_16px_260px_203px] items-start" data-name="Container">
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <Image>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgImage3} />
                </Image>
                <div className="absolute bg-[rgba(0,0,0,0.5)] content-stretch flex items-center justify-center right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
                  <Container />
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="Channel Four" />
                <ContainerText1 text="News" />
              </div>
            </div>
            <div className="absolute content-stretch flex flex-col gap-[12px] inset-[504px_203px_16px_16px] items-start" data-name="Container">
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <Image>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgImage4} />
                </Image>
                <div className="absolute bg-[rgba(0,0,0,0.5)] content-stretch flex items-center justify-center right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
                  <Container />
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="Channel Five" />
                <ContainerText1 text="Main" />
              </div>
            </div>
            <div className="absolute content-stretch flex flex-col gap-[12px] inset-[504px_16px_16px_203px] items-start" data-name="Container">
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <Image>
                  <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgImage5} />
                </Image>
                <div className="absolute bg-[rgba(0,0,0,0.5)] content-stretch flex items-center justify-center right-[8px] rounded-[9999px] size-[32px] top-[8px]" data-name="Button">
                  <Container />
                </div>
              </div>
              <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                <ContainerText text="Channel Six" />
                <ContainerText1 text="Sports" />
              </div>
            </div>
          </div>
        </div>
        <div className="absolute backdrop-blur-[2px] bg-[rgba(16,28,34,0.8)] content-stretch flex flex-col items-start left-0 right-0 top-0" data-name="Header - Top App Bar">
          <div className="relative shrink-0 w-full" data-name="Container">
            <div className="flex flex-row items-center size-full">
              <div className="content-stretch flex items-center p-[16px] relative w-full">
                <div className="content-stretch flex flex-[1_0_0] flex-col items-start min-h-px min-w-px relative" data-name="Heading 1">
                  <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold justify-center leading-[0] not-italic relative shrink-0 text-[20px] text-white tracking-[-0.3px] w-full">
                    <p className="leading-[25px]">⭐ Favorites</p>
                  </div>
                </div>
                <div className="content-stretch flex items-center justify-end relative shrink-0" data-name="Container">
                  <div className="content-stretch flex items-center justify-center relative rounded-[8px] shrink-0 size-[40px]" data-name="Button">
                    <Container1>
                      <path d={svgPaths.p8a35e00} fill="var(--fill-0, #CBD5E1)" id="Icon" />
                    </Container1>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="relative shrink-0 w-full" data-name="Chips">
            <div className="overflow-clip rounded-[inherit] size-full">
              <div className="content-stretch flex gap-[12px] items-start pb-[12px] px-[16px] relative w-full">
                <div className="bg-[#2badee] content-stretch flex gap-[8px] h-[36px] items-center justify-center pl-[16px] pr-[12px] relative rounded-[9999px] shrink-0" data-name="Button">
                  <div className="content-stretch flex flex-col items-center relative shrink-0" data-name="Container">
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[14px] text-center text-white w-[72.67px]">
                      <p className="leading-[21px]">Playlist: All</p>
                    </div>
                  </div>
                  <div className="h-[6.167px] relative shrink-0 w-[10px]" data-name="Container">
                    <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 10 6.16667">
                      <g id="Container">
                        <path d={svgPaths.p3b35c180} fill="var(--fill-0, white)" id="Icon" />
                      </g>
                    </svg>
                  </div>
                </div>
                <div className="bg-[#1e293b] content-stretch flex h-[36px] items-center justify-center px-[16px] relative rounded-[9999px] shrink-0" data-name="Button">
                  <div className="content-stretch flex flex-col items-center relative shrink-0" data-name="Container">
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#cbd5e1] text-[14px] text-center w-[32.69px]">
                      <p className="leading-[21px]">Main</p>
                    </div>
                  </div>
                </div>
                <div className="bg-[#1e293b] content-stretch flex h-[36px] items-center justify-center px-[16px] relative rounded-[9999px] shrink-0" data-name="Button">
                  <div className="content-stretch flex flex-col items-center relative shrink-0" data-name="Container">
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#cbd5e1] text-[14px] text-center w-[43.98px]">
                      <p className="leading-[21px]">Sports</p>
                    </div>
                  </div>
                </div>
                <div className="bg-[#1e293b] content-stretch flex h-[36px] items-center justify-center px-[16px] relative rounded-[9999px] shrink-0" data-name="Button">
                  <div className="content-stretch flex flex-col items-center relative shrink-0" data-name="Container">
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#cbd5e1] text-[14px] text-center w-[29.53px]">
                      <p className="leading-[21px]">Kids</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="absolute backdrop-blur-[2px] bg-[rgba(16,28,34,0.8)] content-stretch flex flex-col items-start left-0 pt-px right-0 top-[809px]" data-name="Bottom Navigation Bar">
          <div aria-hidden="true" className="absolute border-[#1e293b] border-solid border-t inset-0 pointer-events-none" />
          <div className="h-[74px] relative shrink-0 w-full" data-name="Container">
            <div className="flex flex-row justify-center size-full">
              <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex gap-[8px] items-start justify-center pb-[12px] pt-[8px] px-[16px] relative size-full">
                <div className="content-stretch flex flex-[1_0_0] flex-col gap-[4px] items-center justify-end min-h-px min-w-px relative self-stretch" data-name="Link">
                  <div className="content-stretch flex h-[32px] items-center justify-center relative shrink-0" data-name="Container">
                    <div className="h-[18px] relative shrink-0 w-[16px]" data-name="Container">
                      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 16 18">
                        <g id="Container">
                          <path d={svgPaths.p12a32500} fill="var(--fill-0, #94A3B8)" id="Icon" />
                        </g>
                      </svg>
                    </div>
                  </div>
                  <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[18px] justify-center leading-[0] not-italic relative shrink-0 text-[#94a3b8] text-[12px] tracking-[0.18px] w-[34.63px]">
                      <p className="leading-[18px]">Home</p>
                    </div>
                  </div>
                </div>
                <div className="content-stretch flex flex-[1_0_0] flex-col gap-[4px] items-center justify-end min-h-px min-w-px relative self-stretch" data-name="Link">
                  <div className="content-stretch flex h-[32px] items-center justify-center relative shrink-0" data-name="Container">
                    <Container1>
                      <path d={svgPaths.pc94c100} fill="var(--fill-0, #94A3B8)" id="Icon" />
                    </Container1>
                  </div>
                  <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[18px] justify-center leading-[0] not-italic relative shrink-0 text-[#94a3b8] text-[12px] tracking-[0.18px] w-[48.88px]">
                      <p className="leading-[18px]">Playlists</p>
                    </div>
                  </div>
                </div>
                <div className="content-stretch flex flex-[1_0_0] flex-col gap-[4px] items-center justify-end min-h-px min-w-px relative self-stretch" data-name="Link">
                  <div className="content-stretch flex h-[32px] items-center justify-center relative shrink-0" data-name="Container">
                    <div className="h-[19px] relative shrink-0 w-[20px]" data-name="Container">
                      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20 19">
                        <g id="Container">
                          <path d={svgPaths.p3e30af00} fill="var(--fill-0, #2BADEE)" id="Icon" />
                        </g>
                      </svg>
                    </div>
                  </div>
                  <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[18px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[12px] tracking-[0.18px] w-[54.22px]">
                      <p className="leading-[18px]">Favorites</p>
                    </div>
                  </div>
                </div>
                <div className="content-stretch flex flex-[1_0_0] flex-col gap-[4px] items-center justify-end min-h-px min-w-px relative self-stretch" data-name="Link">
                  <div className="content-stretch flex h-[32px] items-center justify-center relative shrink-0" data-name="Container">
                    <div className="h-[20px] relative shrink-0 w-[20.1px]" data-name="Container">
                      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20.1 20">
                        <g id="Container">
                          <path d={svgPaths.p3cdadd00} fill="var(--fill-0, #94A3B8)" id="Icon" />
                        </g>
                      </svg>
                    </div>
                  </div>
                  <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[18px] justify-center leading-[0] not-italic relative shrink-0 text-[#94a3b8] text-[12px] tracking-[0.18px] w-[48.52px]">
                      <p className="leading-[18px]">Settings</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}